package com.divan.service;

import com.divan.entity.ContaAPagar;
import com.divan.repository.ContaAPagarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class ContasVencimentoNotificacaoJob {

    private static final Logger log = LoggerFactory.getLogger(ContasVencimentoNotificacaoJob.class);
    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat REAL_FMT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private static final List<String> NUMEROS_AVISO = List.of(
        "5582996082903",
        "5582996082402"
    );

    @Autowired
    private ContaAPagarRepository contaRepository;

    @Autowired
    private WhatsAppService whatsAppService;

    // ✅ Roda todo dia às 10h
    @Scheduled(cron = "0 0 10 * * *")
    public void notificarContasVencendoHoje() {
        try {
            System.out.println("🔔 [NOTIF] Método chamado - " + LocalDate.now());
            LocalDate hoje = LocalDate.now();
            log.info("📅 Verificando contas com vencimento em: {}", hoje);

            List<ContaAPagar> contasHoje = contaRepository
                .findByDataVencimentoAndStatusNot(hoje, ContaAPagar.StatusContaEnum.PAGA);

            if (contasHoje.isEmpty()) {
                log.info("✅ Nenhuma conta vencendo hoje.");
                System.out.println("✅ [NOTIF] Nenhuma conta vencendo hoje.");
                return;
            }

            System.out.println("📋 [NOTIF] Contas encontradas: " + contasHoje.size());
            String mensagem = montarMensagem(contasHoje, hoje);

            for (String numero : NUMEROS_AVISO) {
                System.out.println("📤 [NOTIF] Enviando para: " + numero);
                WhatsAppService.ResultadoEnvio resultado = whatsAppService.enviarTexto(numero, mensagem);
                if (resultado.isSucesso()) {
                    log.info("✅ Aviso enviado para {}", numero);
                    System.out.println("✅ [NOTIF] Enviado com sucesso para: " + numero);
                } else {
                    log.warn("❌ Falha ao enviar para {}: {}", numero, resultado.getErro());
                    System.out.println("❌ [NOTIF] Falha para " + numero + ": " + resultado.getErro());
                }
            }

        } catch (Exception e) {
            System.out.println("❌ [NOTIF] ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    	
    private String montarMensagem(List<ContaAPagar> contas, LocalDate hoje) {
        BigDecimal totalHoje = contas.stream()
            .map(c -> c.getSaldo() != null ? c.getSaldo() : c.getValor())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder();
        sb.append("🏨 *Hotel Divan — Contas a Pagar*\n\n");
        sb.append("⚠️ *").append(contas.size()).append(" conta(s) vencendo hoje (")
          .append(hoje.format(DATA_FMT)).append("):*\n\n");

        for (ContaAPagar c : contas) {
            sb.append("• ").append(c.getDescricao());
            String nomeFornecedor = c.getFornecedor();
            if (nomeFornecedor == null || nomeFornecedor.isBlank()) {
                if (c.getFornecedorObj() != null) {
                    nomeFornecedor = c.getFornecedorObj().getNome();
                }
            }
            if (nomeFornecedor != null && !nomeFornecedor.isBlank()) {
                sb.append(" — ").append(nomeFornecedor);
            }
            BigDecimal saldo = c.getSaldo() != null ? c.getSaldo() : c.getValor();
            sb.append(" — *").append(REAL_FMT.format(saldo)).append("*\n");
        }

        sb.append("\n*Total:* ").append(REAL_FMT.format(totalHoje));
        sb.append("\n\nAcesse o sistema para registrar os pagamentos. 💰");

        return sb.toString();
    }
}