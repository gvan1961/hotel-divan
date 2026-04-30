package com.divan.service;

import com.divan.entity.Cliente;
import com.divan.entity.Empresa;
import com.divan.entity.HospedagemHospede;
import com.divan.entity.NotificacaoWhatsappLog;
import com.divan.entity.Reserva;
import com.divan.repository.HospedagemHospedeRepository;
import com.divan.repository.NotificacaoWhatsappLogRepository;
import com.divan.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class NotificacaoEmpresaService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoEmpresaService.class);

    public static final String TIPO_CHECKOUT_FATURADO = "CHECKOUT_FATURADO";

    private final WhatsAppService whatsAppService;
    private final HospedagemHospedeRepository hospedagemHospedeRepository;
    private final NotificacaoWhatsappLogRepository logRepository;
    private final ReservaRepository reservaRepository;

    private static final DateTimeFormatter DATA_HORA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat REAL_FMT =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Autowired
    public NotificacaoEmpresaService(WhatsAppService whatsAppService,
                                     HospedagemHospedeRepository hospedagemHospedeRepository,
                                     NotificacaoWhatsappLogRepository logRepository,
                                     ReservaRepository reservaRepository) {
        this.whatsAppService = whatsAppService;
        this.hospedagemHospedeRepository = hospedagemHospedeRepository;
        this.logRepository = logRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Notifica o contato financeiro da empresa quando uma reserva faturada
     * é finalizada (checkout). Roda em thread separada — nunca trava o checkout.
     * Recebe apenas o ID e busca a reserva no banco com sessão fresca.
     */
    @Async
    @Transactional
    public void notificarCheckoutFaturadoAsync(Long reservaId) {
        try {
            Reserva reserva = reservaRepository.findById(reservaId).orElse(null);
            if (reserva == null) {
                log.warn("Reserva {} não encontrada para notificação", reservaId);
                return;
            }
            notificarCheckoutFaturado(reserva);
        } catch (Exception e) {
            log.error("❌ Falha inesperada ao notificar checkout da reserva {}: {}",
                    reservaId, e.getMessage(), e);
        }
    }

    /**
     * Versão síncrona — útil para testes.
     */
    public void notificarCheckoutFaturado(Reserva reserva) {
        if (reserva == null || reserva.getCliente() == null) {
            log.warn("Reserva ou cliente nulo — notificação ignorada");
            return;
        }

        Cliente cliente = reserva.getCliente();
        Empresa empresa = cliente.getEmpresa();

        if (empresa == null) {
            log.info("Reserva {} sem empresa vinculada — notificação ignorada", reserva.getId());
            return;
        }

        log.info("🔍 Empresa: {} (id={}) | DDI: '{}' | Celular: '{}' | Nome contato: '{}'",
                empresa.getNomeEmpresa(),
                empresa.getId(),
                empresa.getContatoFinanceiroDdi(),
                empresa.getContatoFinanceiroCelular(),
                empresa.getContatoFinanceiroNome());

        String numero = whatsAppService.montarNumeroCompleto(
                empresa.getContatoFinanceiroDdi(),
                empresa.getContatoFinanceiroCelular()
        );

        if (numero == null) {
            log.warn("Empresa {} (id={}) sem celular do contato financeiro cadastrado",
                    empresa.getNomeEmpresa(), empresa.getId());
            salvarLog(TIPO_CHECKOUT_FATURADO, "(sem número)", null, null,
                    false, "Empresa sem celular do contato financeiro", null,
                    reserva.getId(), empresa.getId());
            return;
        }

        // Evita envio duplicado
        List<NotificacaoWhatsappLog> jaEnviados =
                logRepository.findByTipoAndReservaIdAndSucessoTrue(
                        TIPO_CHECKOUT_FATURADO, reserva.getId());
        if (!jaEnviados.isEmpty()) {
            log.info("Reserva {} já possui notificação de checkout enviada — ignorando",
                    reserva.getId());
            return;
        }

        String mensagem = montarMensagem(reserva, empresa, cliente);

        WhatsAppService.ResultadoEnvio resultado = whatsAppService.enviarTexto(numero, mensagem);

        salvarLog(TIPO_CHECKOUT_FATURADO,
                numero,
                empresa.getContatoFinanceiroNome(),
                mensagem,
                resultado.isSucesso(),
                resultado.getErro(),
                resultado.getResponseBody(),
                reserva.getId(),
                empresa.getId());
    }

    private String montarMensagem(Reserva reserva, Empresa empresa, Cliente cliente) {
        List<HospedagemHospede> hospedes =
                hospedagemHospedeRepository.findByReservaId(reserva.getId());

        StringBuilder listaHospedes = new StringBuilder();
        for (HospedagemHospede h : hospedes) {
            if (h.getCliente() != null) {
                listaHospedes.append("• ").append(h.getCliente().getNome()).append("\n");
            }
        }

        long qtdDiarias = calcularDiarias(reserva);

        String numeroApto = reserva.getApartamento() != null
                ? reserva.getApartamento().getNumeroApartamento()
                : "—";

        String dataIn = reserva.getDataCheckin() != null
                ? reserva.getDataCheckin().format(DATA_HORA_FMT) : "—";
        String dataOut = reserva.getDataCheckout() != null
                ? reserva.getDataCheckout().format(DATA_HORA_FMT) : "—";

        BigDecimal totalHospedagem = reserva.getTotalHospedagem() != null
                ? reserva.getTotalHospedagem() : BigDecimal.ZERO;

        StringBuilder sb = new StringBuilder();
        sb.append("🏨 *Hotel Divan — Checkout Realizado*\n\n");
        sb.append("Olá");
        if (empresa.getContatoFinanceiroNome() != null
                && !empresa.getContatoFinanceiroNome().isBlank()) {
            sb.append(", ").append(empresa.getContatoFinanceiroNome());
        }
        sb.append("!\n\n");
        sb.append("Informamos o checkout faturado da reserva abaixo:\n\n");
        sb.append("*Empresa:* ").append(empresa.getNomeEmpresa()).append("\n");
        sb.append("*Reserva:* #").append(reserva.getId()).append("\n");
        sb.append("*Apartamento:* ").append(numeroApto).append("\n");
        sb.append("*Check-in:* ").append(dataIn).append("\n");
        sb.append("*Check-out:* ").append(dataOut).append("\n");
        sb.append("*Diárias:* ").append(qtdDiarias).append("\n\n");

        if (listaHospedes.length() > 0) {
            sb.append("*Hóspedes:*\n").append(listaHospedes).append("\n");
        }

        sb.append("*Valor total:* ").append(REAL_FMT.format(totalHospedagem)).append("\n\n");
        sb.append("Obrigado pela preferência! 🙏");

        return sb.toString();
    }

    private long calcularDiarias(Reserva reserva) {
        if (reserva.getDataCheckin() == null || reserva.getDataCheckout() == null) {
            return 0;
        }
        long horas = Duration.between(reserva.getDataCheckin(), reserva.getDataCheckout()).toHours();
        long diarias = horas / 24;
        if (horas % 24 > 0) diarias++;
        return Math.max(diarias, 1);
    }

    private void salvarLog(String tipo, String destinatario, String nome, String mensagem,
                           boolean sucesso, String erro, String response,
                           Long reservaId, Long empresaId) {
        try {
            NotificacaoWhatsappLog logEntry = new NotificacaoWhatsappLog();
            logEntry.setTipo(tipo);
            logEntry.setDestinatario(destinatario);
            logEntry.setNomeDestinatario(nome);
            logEntry.setMensagem(mensagem);
            logEntry.setSucesso(sucesso);
            logEntry.setErro(erro);
            logEntry.setResponseEvolution(response);
            logEntry.setReservaId(reservaId);
            logEntry.setEmpresaId(empresaId);
            logEntry.setDataEnvio(LocalDateTime.now());

            logRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Erro ao gravar log de notificação WhatsApp: {}", e.getMessage(), e);
        }
    }
}
