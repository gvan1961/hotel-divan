package com.divan.service;

import com.divan.entity.Apartamento;
import com.divan.entity.Cliente;
import com.divan.entity.Diaria;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.ClienteRepository;
import com.divan.repository.DiariaRepository;
import com.divan.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.divan.repository.SolicitacaoReservaWhatsappRepository;
import com.divan.entity.SolicitacaoReservaWhatsapp;

@Service
public class WhatsAppReservaService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppReservaService.class);
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat REAL_FMT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final String NUMERO_RECEPCAO = "558291033057";

    private final Map<String, SessaoConversa> sessoes = new ConcurrentHashMap<>();

    @Autowired private WhatsAppService whatsAppService;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ApartamentoRepository apartamentoRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private DiariaRepository diariaRepository;
    @Autowired private SolicitacaoReservaWhatsappRepository solicitacaoRepository;

    private enum Estado {
        INICIO,
        AGUARDANDO_NOME,
        AGUARDANDO_CHECKIN,
        AGUARDANDO_CHECKOUT,
        AGUARDANDO_HOSPEDES,
        AGUARDANDO_CONFIRMACAO,
        AGUARDANDO_DATA_DISPONIBILIDADE
    }

    private static class SessaoConversa {
        Estado estado = Estado.INICIO;
        String nome;
        String cpf;
        LocalDate checkin;
        LocalDate checkout;
        int hospedes = 1;
        LocalDateTime ultimaAtividade = LocalDateTime.now();

        boolean expirada() {
            return LocalDateTime.now().isAfter(ultimaAtividade.plusHours(2));
        }
        void atualizar() { ultimaAtividade = LocalDateTime.now(); }
    }

    @Async
    public void processarMensagemAsync(String numero, String texto) {
        try {
            processarMensagem(numero, texto.trim());
        } catch (Exception e) {
            log.error("❌ Erro ao processar mensagem de {}: {}", numero, e.getMessage(), e);
            enviar(numero, "❌ Ocorreu um erro. Por favor, tente novamente ou ligue para o hotel.");
        }
    }

    private void processarMensagem(String numero, String texto) {
        sessoes.entrySet().removeIf(e -> e.getValue().expirada());

        SessaoConversa sessao = sessoes.computeIfAbsent(numero, k -> new SessaoConversa());
        sessao.atualizar();

        String textoLower = texto.toLowerCase().trim();

        // Comandos globais
        if (textoLower.equals("cancelar") || textoLower.equals("sair") || textoLower.equals("0")) {
            sessoes.remove(numero);
            enviar(numero, "✅ Atendimento encerrado. Digite *oi* quando precisar! 😊");
            return;
        }
        if (textoLower.equals("menu") || textoLower.equals("inicio") || textoLower.equals("início")) {
            sessao.estado = Estado.INICIO;
        }

        switch (sessao.estado) {
            case INICIO                      -> processarInicio(numero, textoLower, sessao);
            case AGUARDANDO_NOME             -> processarNome(numero, texto, sessao);
            case AGUARDANDO_CHECKIN          -> processarCheckin(numero, texto, sessao);
            case AGUARDANDO_CHECKOUT         -> processarCheckout(numero, texto, sessao);
            case AGUARDANDO_HOSPEDES         -> processarHospedes(numero, texto, sessao);
            case AGUARDANDO_CONFIRMACAO      -> processarConfirmacao(numero, textoLower, sessao);
            case AGUARDANDO_DATA_DISPONIBILIDADE -> processarDataDisponibilidade(numero, texto, sessao);
        }
    }

    private void processarInicio(String numero, String texto, SessaoConversa sessao) {
        boolean eSaudacao = texto.contains("oi") || texto.contains("olá") || texto.contains("ola")
                || texto.contains("bom dia") || texto.contains("boa tarde")
                || texto.contains("boa noite") || texto.contains("hello")
                || texto.equals("menu") || texto.equals("início") || texto.equals("inicio");

        if (eSaudacao) {
            enviarMenu(numero);
            return;
        }

        switch (texto) {
            case "1" -> iniciarSolicitacao(numero, sessao);
            case "2" -> {
                sessao.estado = Estado.AGUARDANDO_DATA_DISPONIBILIDADE;
                enviar(numero,
                    "📅 *Consultar Disponibilidade*\n\n" +
                    "Informe a data que deseja verificar no formato *DD/MM/AAAA*:\n_(Ex: 25/06/2026)_");
            }
            case "3" -> {
                enviarPrecos(numero);
                sessoes.remove(numero);
            }
            case "4" -> {
                enviarInformacoesHotel(numero);
                sessoes.remove(numero);
            }
            case "5" -> {
                enviar(numero,
                    "📞 *Falar com a Recepção*\n\n" +
                    "Nossa equipe está à disposição!\n\n" +
                    "📍 Arapiraca - AL\n" +
                    "⏰ Atendimento 24h\n\n" +
                    "Em breve um recepcionista entrará em contato. 😊");
                sessoes.remove(numero);
            }
            default -> enviarMenu(numero);
        }
    }

    private void enviarMenu(String numero) {
        enviar(numero,
            "🏨 *Hotel Di Van - Arapiraca/AL*\n\n" +
            "Olá! Bem-vindo(a)! Como posso ajudar?\n\n" +
            "*1* - 🛏️ Solicitar reserva\n" +
            "*2* - 📅 Verificar disponibilidade\n" +
            "*3* - 💰 Preços das diárias\n" +
            "*4* - ℹ️ Informações do hotel\n" +
            "*5* - 📞 Falar com a recepção\n\n" +
            "_Digite_ *cancelar* _a qualquer momento para encerrar._");
    }

    private void enviarPrecos(String numero) {
        try {
            List<Diaria> diarias = diariaRepository.findAll();

            if (diarias.isEmpty()) {
                enviar(numero,
                    "💰 *Tabela de Preços*\n\n" +
                    "Entre em contato com a recepção para obter os preços atualizados.\n" +
                    "📞 Atendimento 24h");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("💰 *Tabela de Preços - Hotel Di Van*\n\n");

            // Agrupa por tipo de apartamento
            diarias.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    d -> d.getTipoApartamento().getTipo().name()))
                .forEach((tipo, lista) -> {
                    sb.append("🏨 *Tipo ").append(tipo).append("*\n");
                    lista.stream()
                        .sorted((a, b) -> Integer.compare(a.getQuantidade(), b.getQuantidade()))
                        .forEach(d -> {
                            sb.append("   👥 ").append(d.getQuantidade()).append(" pessoa(s): *")
                              .append(REAL_FMT.format(d.getValor())).append("/noite*\n");
                        });
                    sb.append("\n");
                });

            sb.append("━━━━━━━━━━━━━━━━\n");
            sb.append("ℹ️ _Preços sujeitos a alteração._\n");
            sb.append("📞 _Para reservas, escolha a opção 1 no menu._");

            enviar(numero, sb.toString());

        } catch (Exception e) {
            log.error("Erro ao buscar preços: {}", e.getMessage());
            enviar(numero, "💰 Entre em contato com a recepção para obter os preços atualizados.");
        }
    }

    private void processarDataDisponibilidade(String numero, String texto, SessaoConversa sessao) {
        try {
            LocalDate data = LocalDate.parse(texto.trim(), FMT_DATA);

            if (data.isBefore(LocalDate.now())) {
                enviar(numero, "⚠️ Informe uma data a partir de hoje:");
                return;
            }

            LocalDateTime inicio = data.atStartOfDay();
            LocalDateTime fim = data.plusDays(1).atStartOfDay();

            // Conta apartamentos disponíveis para essa data
            List<Apartamento> todos = apartamentoRepository.findAll();
            long ocupados = reservaRepository.findAll().stream()
                .filter(r -> r.getStatus() == Reserva.StatusReservaEnum.ATIVA
                          || r.getStatus() == Reserva.StatusReservaEnum.PRE_RESERVA)
                .filter(r -> r.getDataCheckin().isBefore(fim) && r.getDataCheckout().isAfter(inicio))
                .map(r -> r.getApartamento().getId())
                .distinct()
                .count();

            long disponiveis = todos.size() - ocupados;

            StringBuilder sb = new StringBuilder();
            sb.append("📅 *Disponibilidade para ").append(data.format(FMT_DATA)).append("*\n\n");

            if (disponiveis > 0) {
                sb.append("✅ Temos *").append(disponiveis).append("* apartamento(s) disponível(is)!\n\n");
                sb.append("Para solicitar uma reserva, entre em contato com nossa recepção ou escolha a opção *1* no menu.\n\n");
            } else {
                sb.append("😔 Não há apartamentos disponíveis para esta data.\n\n");
                sb.append("Tente outras datas ou fale com nossa recepção.\n\n");
            }

            sb.append("━━━━━━━━━━━━━━━━\n");
            sb.append("Digite *menu* para voltar ao menu principal.");

            enviar(numero, sb.toString());
            sessao.estado = Estado.INICIO;

        } catch (DateTimeParseException e) {
            enviar(numero, "⚠️ Data inválida. Use o formato *DD/MM/AAAA*:\n_(Ex: 25/06/2026)_");
        }
    }

    private void enviarInformacoesHotel(String numero) {
        enviar(numero,
            "ℹ️ *Hotel Di Van*\n\n" +
            "📍 *Localização:* Arapiraca - AL\n\n" +
            "⏰ *Horários:*\n" +
            "   🟢 Check-in: a partir das 12h\n" +
            "   🔴 Check-out: até às 12h\n\n" +
            "🏨 *Comodidades:*\n" +
            "   • Ar-condicionado\n" +
            "   • TV Smart\n" +
            "   • Wi-Fi\n" +
            "   • Estacionamento\n\n" +
            "📞 *Atendimento 24h*\n\n" +
            "_Digite_ *menu* _para voltar ao menu principal._");
    }

    private void iniciarSolicitacao(String numero, SessaoConversa sessao) {
        // Tenta identificar pelo número
        Optional<Cliente> clienteOpt = clienteRepository.findByCelularCompleto(numero);

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            sessao.nome = cliente.getNome();
            sessao.cpf = cliente.getCpf();
            sessao.estado = Estado.AGUARDANDO_CHECKIN;
            enviar(numero,
                "😊 Olá, *" + cliente.getNome() + "*!\n\n" +
                "📅 *Qual a data de Check-in?*\n\n" +
                "Informe no formato *DD/MM/AAAA*:\n_(Ex: 25/06/2026)_");
        } else {
            sessao.estado = Estado.AGUARDANDO_NOME;
            enviar(numero,
                "📋 *Solicitação de Reserva*\n\n" +
                "Por favor, informe seu *nome completo*:");
        }
    }

    private void processarNome(String numero, String texto, SessaoConversa sessao) {
        if (texto.trim().length() < 3) {
            enviar(numero, "⚠️ Por favor, informe seu nome completo:");
            return;
        }
        sessao.nome = texto.trim();
        sessao.estado = Estado.AGUARDANDO_CHECKIN;
        enviar(numero,
            "📅 *Qual a data de Check-in?*\n\n" +
            "Informe no formato *DD/MM/AAAA*:\n_(Ex: 25/06/2026)_");
    }

    private void processarCheckin(String numero, String texto, SessaoConversa sessao) {
        try {
            LocalDate checkin = LocalDate.parse(texto.trim(), FMT_DATA);
            if (checkin.isBefore(LocalDate.now())) {
                enviar(numero, "⚠️ A data de check-in não pode ser no passado. Informe uma data a partir de hoje:");
                return;
            }
            sessao.checkin = checkin;
            sessao.estado = Estado.AGUARDANDO_CHECKOUT;
            enviar(numero,
                "📅 *Qual a data de Check-out?*\n\n" +
                "Informe no formato *DD/MM/AAAA*:\n_(Ex: 27/06/2026)_");
        } catch (DateTimeParseException e) {
            enviar(numero, "⚠️ Data inválida. Use o formato *DD/MM/AAAA*:\n_(Ex: 25/06/2026)_");
        }
    }

    private void processarCheckout(String numero, String texto, SessaoConversa sessao) {
        try {
            LocalDate checkout = LocalDate.parse(texto.trim(), FMT_DATA);
            if (!checkout.isAfter(sessao.checkin)) {
                enviar(numero, "⚠️ O check-out deve ser após o check-in (" + sessao.checkin.format(FMT_DATA) + "). Informe outra data:");
                return;
            }
            sessao.checkout = checkout;
            sessao.estado = Estado.AGUARDANDO_HOSPEDES;
            enviar(numero, "👥 *Quantas pessoas vão se hospedar?*\n\nInforme o número de hóspedes:");
        } catch (DateTimeParseException e) {
            enviar(numero, "⚠️ Data inválida. Use o formato *DD/MM/AAAA*:\n_(Ex: 27/06/2026)_");
        }
    }

    private void processarHospedes(String numero, String texto, SessaoConversa sessao) {
        try {
            int hospedes = Integer.parseInt(texto.trim());
            if (hospedes < 1 || hospedes > 10) {
                enviar(numero, "⚠️ Informe um número entre 1 e 10 hóspedes:");
                return;
            }
            sessao.hospedes = hospedes;
            sessao.estado = Estado.AGUARDANDO_CONFIRMACAO;

            long dias = sessao.checkin.until(sessao.checkout).getDays();

            enviar(numero,
                "📋 *Resumo da Solicitação:*\n" +
                "━━━━━━━━━━━━━━━━\n" +
                "👤 Nome: *" + sessao.nome + "*\n" +
                "📅 Check-in: *" + sessao.checkin.format(FMT_DATA) + "*\n" +
                "📅 Check-out: *" + sessao.checkout.format(FMT_DATA) + "*\n" +
                "🌙 Diárias: *" + dias + "*\n" +
                "👥 Hóspedes: *" + hospedes + "*\n" +
                "━━━━━━━━━━━━━━━━\n\n" +
                "Confirma o envio da solicitação?\n\n" +
                "*1* - ✅ Sim, enviar\n" +
                "*2* - ❌ Não, cancelar");
        } catch (NumberFormatException e) {
            enviar(numero, "⚠️ Informe apenas o número de hóspedes (Ex: *2*):");
        }
    }

    private void processarConfirmacao(String numero, String texto, SessaoConversa sessao) {
    	if (texto.equals("1") || texto.equals("sim") || texto.equals("s")) {
    	    salvarSolicitacao(numero, sessao);
    	    notificarRecepcao(numero, sessao);
            enviar(numero,
                "✅ *Solicitação enviada com sucesso!*\n\n" +
                "Nossa recepção recebeu seu pedido e entrará em contato em breve para confirmar a disponibilidade e finalizar sua reserva.\n\n" +
                "Obrigado pela preferência! 🙏\n" +
                "_Hotel Di Van - Arapiraca/AL_");
            sessoes.remove(numero);
        } else if (texto.equals("2") || texto.startsWith("n")) {
            sessoes.remove(numero);
            enviar(numero, "✅ Solicitação cancelada. Digite *oi* para voltar ao menu.");
        } else {
            enviar(numero, "Por favor, responda *1* para confirmar ou *2* para cancelar:");
        }
    }
    
    private void salvarSolicitacao(String numeroCliente, SessaoConversa sessao) {
        try {
            SolicitacaoReservaWhatsapp s = new SolicitacaoReservaWhatsapp();
            s.setNome(sessao.nome);
            s.setCpf(sessao.cpf);
            s.setNumeroWhatsapp(numeroCliente);
            s.setDataCheckin(sessao.checkin);
            s.setDataCheckout(sessao.checkout);
            s.setQuantidadeHospedes(sessao.hospedes);
            solicitacaoRepository.save(s);
            log.info("✅ Solicitação salva no banco: {}", sessao.nome);
        } catch (Exception e) {
            log.error("❌ Erro ao salvar solicitação: {}", e.getMessage());
        }
    }

    private void notificarRecepcao(String numeroCliente, SessaoConversa sessao) {
        long dias = sessao.checkin.until(sessao.checkout).getDays();
        String msg =
            "🔔 *Nova Solicitação de Reserva via WhatsApp*\n\n" +
            "👤 Nome: *" + sessao.nome + "*\n" +
            (sessao.cpf != null ? "🪪 CPF: " + sessao.cpf + "\n" : "") +
            "📱 WhatsApp: *" + numeroCliente + "*\n" +
            "📅 Check-in: *" + sessao.checkin.format(FMT_DATA) + "*\n" +
            "📅 Check-out: *" + sessao.checkout.format(FMT_DATA) + "*\n" +
            "🌙 Diárias: *" + dias + "*\n" +
            "👥 Hóspedes: *" + sessao.hospedes + "*\n\n" +
            "⚠️ Entre em contato para verificar disponibilidade e confirmar!";

        whatsAppService.enviarTexto(NUMERO_RECEPCAO, msg);
        log.info("📲 Solicitação de {} notificada para recepção", sessao.nome);
    }

    private void enviar(String numero, String mensagem) {
        whatsAppService.enviarTexto(numero, mensagem);
    }
}