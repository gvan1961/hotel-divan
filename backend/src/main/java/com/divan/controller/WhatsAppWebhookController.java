package com.divan.controller;

import com.divan.service.WhatsAppReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/publico/webhook")
@CrossOrigin(origins = "*")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Autowired
    private WhatsAppReservaService whatsAppReservaService;

    @PostMapping("/whatsapp")
    public ResponseEntity<?> receberMensagem(@RequestBody Map<String, Object> payload) {
        try {
            log.info("📩 Webhook recebido: {}", payload.get("event"));

            String evento = payload.get("event") != null ? payload.get("event").toString() : "";

            // Só processa mensagens recebidas
            if (!"messages.upsert".equals(evento)) {
                return ResponseEntity.ok(Map.of("status", "ignorado"));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null) return ResponseEntity.ok(Map.of("status", "sem dados"));

            // Ignora mensagens enviadas pelo próprio bot
            Object fromMe = data.get("key") != null
                ? ((Map<?, ?>) data.get("key")).get("fromMe") : null;
            if (Boolean.TRUE.equals(fromMe)) {
                return ResponseEntity.ok(Map.of("status", "ignorado - proprio bot"));
            }

            // Extrai número do remetente
            Object keyObj = data.get("key");
            if (!(keyObj instanceof Map)) return ResponseEntity.ok(Map.of("status", "sem key"));
            String remoteJid = ((Map<?, ?>) keyObj).get("remoteJid") != null
                ? ((Map<?, ?>) keyObj).get("remoteJid").toString() : null;
            if (remoteJid == null) return ResponseEntity.ok(Map.of("status", "sem numero"));

            // Extrai texto da mensagem
            String texto = extrairTexto(data);
            if (texto == null || texto.isBlank()) {
                return ResponseEntity.ok(Map.of("status", "sem texto"));
            }

            // Extrai número limpo (remove @s.whatsapp.net)
            String numero = remoteJid.replace("@s.whatsapp.net", "").replace("@g.us", "");

            log.info("📱 Mensagem de {}: {}", numero, texto);

            // Processa a mensagem em thread separada para não travar o webhook
            whatsAppReservaService.processarMensagemAsync(numero, texto);

            return ResponseEntity.ok(Map.of("status", "processando"));

        } catch (Exception e) {
            log.error("❌ Erro no webhook WhatsApp: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("status", "erro", "detalhe", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private String extrairTexto(Map<String, Object> data) {
        try {
            Object msgObj = data.get("message");
            if (!(msgObj instanceof Map)) return null;
            Map<String, Object> message = (Map<String, Object>) msgObj;

            // Texto simples
            if (message.get("conversation") != null) {
                return message.get("conversation").toString();
            }

            // Texto estendido
            if (message.get("extendedTextMessage") instanceof Map) {
                Map<String, Object> ext = (Map<String, Object>) message.get("extendedTextMessage");
                if (ext.get("text") != null) return ext.get("text").toString();
            }

            // Legenda de imagem/vídeo
            if (message.get("imageMessage") instanceof Map) {
                Map<String, Object> img = (Map<String, Object>) message.get("imageMessage");
                if (img.get("caption") != null) return img.get("caption").toString();
            }

            return null;
        } catch (Exception e) {
            log.warn("Erro ao extrair texto da mensagem: {}", e.getMessage());
            return null;
        }
    }
}
