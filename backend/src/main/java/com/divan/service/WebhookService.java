package com.divan.service;

import com.divan.entity.Webhook;
import com.divan.entity.WebhookEvento;
import com.divan.entity.WebhookSubscription;
import com.divan.repository.WebhookRepository;
import com.divan.repository.WebhookSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebhookService {

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private WebhookSubscriptionRepository subscriptionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    // ════════════════════════════════════════════════════════════
    // DISPARAR WEBHOOKS
    // ════════════════════════════════════════════════════════════

    /**
     * Dispara webhooks para um evento específico
     * 
     * @param evento Tipo do evento
     * @param dados Dados a serem enviados no payload
     */
    @Transactional
    public void disparar(WebhookEvento evento, Map<String, Object> dados) {
        disparar(evento.name(), dados, null, null);
    }

    /**
     * Dispara webhooks para um evento com informação de entidade
     * 
     * @param evento Tipo do evento
     * @param dados Dados a serem enviados no payload
     * @param entidadeTipo Tipo da entidade (Reserva, Pagamento, etc)
     * @param entidadeId ID da entidade
     */
    @Transactional
    public void disparar(WebhookEvento evento, Map<String, Object> dados, String entidadeTipo, Long entidadeId) {
        disparar(evento.name(), dados, entidadeTipo, entidadeId);
    }

    /**
     * Dispara webhooks (método interno)
     */
    @Transactional
    public void disparar(String evento, Map<String, Object> dados, String entidadeTipo, Long entidadeId) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📤 DISPARANDO WEBHOOKS");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📋 Evento: " + evento);
        System.out.println("📦 Dados: " + dados);
        if (entidadeTipo != null && entidadeId != null) {
            System.out.println("🔗 Entidade: " + entidadeTipo + " #" + entidadeId);
        }

        // Buscar subscriptions ativas para este evento
        List<WebhookSubscription> subscriptions = subscriptionRepository
            .findByEventoAndAtivoTrue(evento);

        if (subscriptions.isEmpty()) {
            System.out.println("⚠️ Nenhuma subscription ativa encontrada para o evento: " + evento);
            System.out.println("═══════════════════════════════════════════════════════════");
            return;
        }

        System.out.println("✅ Encontradas " + subscriptions.size() + " subscription(s) ativa(s)");

        // Criar payload completo
        Map<String, Object> payloadCompleto = new HashMap<>();
        payloadCompleto.put("evento", evento);
        payloadCompleto.put("timestamp", LocalDateTime.now().toString());
        payloadCompleto.put("dados", dados);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadCompleto);
        } catch (Exception e) {
            System.err.println("❌ Erro ao serializar payload: " + e.getMessage());
            return;
        }

        // Criar webhook para cada subscription
        for (WebhookSubscription subscription : subscriptions) {
            System.out.println("\n📌 Criando webhook para: " + subscription.getNome());
            System.out.println("   URL: " + subscription.getUrl());

            Webhook webhook = new Webhook();
            webhook.setSubscription(subscription);
            webhook.setEvento(evento);
            webhook.setUrlDestino(subscription.getUrl());
            webhook.setPayload(payloadJson);
            webhook.setStatus(Webhook.StatusWebhookEnum.PENDING);
            webhook.setTentativas(0);
            webhook.setMaxTentativas(subscription.getMaxTentativas());
            webhook.setEntidadeTipo(entidadeTipo);
            webhook.setEntidadeId(entidadeId);

            Webhook salvo = webhookRepository.save(webhook);

            System.out.println("   ✅ Webhook criado: ID " + salvo.getId());

            // Enviar assincronamente
            enviarAsync(salvo.getId());
        }

        System.out.println("═══════════════════════════════════════════════════════════");
    }

    // ════════════════════════════════════════════════════════════
    // ENVIO DE WEBHOOKS
    // ════════════════════════════════════════════════════════════

    /**
     * Envia webhook de forma assíncrona
     */
    @Async
    @Transactional
    public void enviarAsync(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new RuntimeException("Webhook não encontrado: " + webhookId));

        enviar(webhook);
    }

    /**
     * Envia um webhook
     */
    @Transactional
    public void enviar(Webhook webhook) {
        System.out.println("\n🚀 ENVIANDO WEBHOOK #" + webhook.getId());
        System.out.println("   Evento: " + webhook.getEvento());
        System.out.println("   URL: " + webhook.getUrlDestino());
        System.out.println("   Tentativa: " + (webhook.getTentativas() + 1) + "/" + webhook.getMaxTentativas());

        // Atualizar status
        webhook.setStatus(Webhook.StatusWebhookEnum.SENDING);
        webhook.setTentativas(webhook.getTentativas() + 1);
        webhook.setUltimaTentativa(LocalDateTime.now());
        webhookRepository.save(webhook);

        long inicio = System.currentTimeMillis();

        try {
            // Construir requisição HTTP
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(webhook.getUrlDestino()))
                .timeout(Duration.ofSeconds(
                    webhook.getSubscription() != null 
                        ? webhook.getSubscription().getTimeoutSegundos() 
                        : 30
                ))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Hotel-Divan-Webhook/1.0");

            // Adicionar headers customizados da subscription
            if (webhook.getSubscription() != null && webhook.getSubscription().getHeaders() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> headers = objectMapper.readValue(
                        webhook.getSubscription().getHeaders(), 
                        Map.class
                    );
                    headers.forEach(requestBuilder::header);
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao processar headers customizados: " + e.getMessage());
                }
            }

            // Adicionar assinatura HMAC se houver secret
            if (webhook.getSubscription() != null && webhook.getSubscription().getSecret() != null) {
                String signature = gerarAssinatura(webhook.getPayload(), webhook.getSubscription().getSecret());
                requestBuilder.header("X-Webhook-Signature", signature);
            }

            // Definir método e body
            String metodo = webhook.getSubscription() != null 
                ? webhook.getSubscription().getMetodoHttp() 
                : "POST";

            HttpRequest request = requestBuilder
                .method(metodo, HttpRequest.BodyPublishers.ofString(webhook.getPayload()))
                .build();

            // Enviar requisição
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long tempoResposta = System.currentTimeMillis() - inicio;

            // Processar resposta
            webhook.setRespostaHttp(response.statusCode());
            webhook.setRespostaBody(response.body());
            webhook.setTempoRespostaMs(tempoResposta);

            System.out.println("   📊 Status HTTP: " + response.statusCode());
            System.out.println("   ⏱️ Tempo: " + tempoResposta + "ms");

            // Verificar se foi sucesso (2xx)
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                webhook.setStatus(Webhook.StatusWebhookEnum.SUCCESS);
                System.out.println("   ✅ SUCESSO!");
            } else {
                tratarFalha(webhook, "HTTP " + response.statusCode() + ": " + response.body());
            }

        } catch (Exception e) {
            long tempoResposta = System.currentTimeMillis() - inicio;
            webhook.setTempoRespostaMs(tempoResposta);
            
            System.err.println("   ❌ ERRO: " + e.getMessage());
            tratarFalha(webhook, e.getMessage());
        }

        webhookRepository.save(webhook);
    }

    /**
     * Trata falha no envio do webhook
     */
    private void tratarFalha(Webhook webhook, String mensagemErro) {
        webhook.setErroMensagem(mensagemErro);

        if (webhook.getTentativas() >= webhook.getMaxTentativas()) {
            // Esgotou tentativas
            webhook.setStatus(Webhook.StatusWebhookEnum.FAILED);
            webhook.setProximaTentativa(null);
            
            System.out.println("   ❌ FALHOU - Tentativas esgotadas");
        } else {
            // Agendar próxima tentativa (backoff exponencial)
            webhook.setStatus(Webhook.StatusWebhookEnum.PENDING);
            int minutosEspera = (int) Math.pow(2, webhook.getTentativas()); // 2, 4, 8, 16...
            webhook.setProximaTentativa(LocalDateTime.now().plusMinutes(minutosEspera));
            
            System.out.println("   ⏰ Próxima tentativa em " + minutosEspera + " minuto(s)");
        }
    }

    /**
     * Gera assinatura HMAC-SHA256
     */
    private String gerarAssinatura(String payload, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(), "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            
            // Converter para hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("❌ Erro ao gerar assinatura: " + e.getMessage());
            return "";
        }
    }

    // ════════════════════════════════════════════════════════════
    // REENVIO AUTOMÁTICO (SCHEDULED)
    // ════════════════════════════════════════════════════════════

    /**
     * Job agendado para reenviar webhooks pendentes
     * Executa a cada 1 minuto
     */
    @Scheduled(fixedRate = 60000) // 60 segundos
    @Transactional
    public void reenviarPendentes() {
        List<Webhook> pendentes = webhookRepository.findPendentesParaReenvio(LocalDateTime.now());

        if (!pendentes.isEmpty()) {
            System.out.println("\n🔄 REENVIO AUTOMÁTICO: " + pendentes.size() + " webhook(s) pendente(s)");

            for (Webhook webhook : pendentes) {
                try {
                    enviar(webhook);
                } catch (Exception e) {
                    System.err.println("❌ Erro ao reenviar webhook #" + webhook.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ════════════════════════════════════════════════════════════

    /**
     * Cancela um webhook pendente
     */
    @Transactional
    public void cancelar(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new RuntimeException("Webhook não encontrado"));

        webhook.setStatus(Webhook.StatusWebhookEnum.CANCELLED);
        webhook.setProximaTentativa(null);
        webhookRepository.save(webhook);

        System.out.println("❌ Webhook #" + webhookId + " cancelado");
    }

    /**
     * Reenviar webhook manualmente
     */
    @Transactional
    public void reenviar(Long webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new RuntimeException("Webhook não encontrado"));

        // Reset
        webhook.setTentativas(0);
        webhook.setStatus(Webhook.StatusWebhookEnum.PENDING);
        webhook.setProximaTentativa(LocalDateTime.now());
        webhook.setErroMensagem(null);
        webhookRepository.save(webhook);

        enviarAsync(webhookId);
    }

    /**
     * Busca histórico de webhooks de uma entidade
     */
    public List<Webhook> buscarHistorico(String entidadeTipo, Long entidadeId) {
        return webhookRepository.findByEntidadaTipoAndEntidadeId(entidadeTipo, entidadeId);
    }

    /**
     * Estatísticas gerais
     */
    public Map<String, Object> estatisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("pendentes", webhookRepository.countByStatus(Webhook.StatusWebhookEnum.PENDING));
        stats.put("enviando", webhookRepository.countByStatus(Webhook.StatusWebhookEnum.SENDING));
        stats.put("sucesso", webhookRepository.countByStatus(Webhook.StatusWebhookEnum.SUCCESS));
        stats.put("falhados", webhookRepository.countByStatus(Webhook.StatusWebhookEnum.FAILED));
        stats.put("cancelados", webhookRepository.countByStatus(Webhook.StatusWebhookEnum.CANCELLED));
        
        stats.put("subscriptions_ativas", subscriptionRepository.countByAtivoTrue());
        
        stats.put("ultimos_10", webhookRepository.findTop10ByOrderByCriadoEmDesc());
        
        return stats;
    }

    /**
     * Limpar webhooks antigos (manutenção)
     * Remove webhooks com mais de X dias
     */
    @Transactional
    public void limparAntigos(int dias) {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(dias);
        
        System.out.println("🧹 Limpando webhooks anteriores a: " + dataLimite);
        
        webhookRepository.deleteByCriadoEmBefore(dataLimite);
        
        System.out.println("✅ Limpeza concluída");
    }
}
