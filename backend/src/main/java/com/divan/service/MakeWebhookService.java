package com.divan.service;

import com.divan.entity.Reserva;
import com.divan.entity.Webhook;
import com.divan.repository.WebhookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MakeWebhookService {
    
    @Autowired
    private WebhookRepository webhookRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${make.webhook.url}")
    private String makeWebhookUrl;
    
    @Value("${make.webhook.enabled:true}")
    private boolean makeEnabled;
    
    /**
     * 📤 ENVIAR EVENTO PARA O MAKE
     */
    public void enviarEvento(String evento, Map<String, Object> dados) {
        if (!makeEnabled) {
            System.out.println("⚠️ Make.com desabilitado");
            return;
        }
        
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📤 ENVIANDO EVENTO PARA MAKE.COM");
        System.out.println("   Evento: " + evento);
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("evento", evento);
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("dados", dados);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                makeWebhookUrl, 
                request, 
                String.class
            );
            
            System.out.println("✅ Evento enviado! Status: " + response.getStatusCode());
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar para Make: " + e.getMessage());
        }
        
        System.out.println("═══════════════════════════════════════════");
    }
    
    /**
     * 🏨 NOVA RESERVA
     */
    public void notificarNovaReserva(Reserva reserva) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("reserva_id", reserva.getId());
        dados.put("cliente_nome", reserva.getCliente().getNome());
        dados.put("cliente_telefone", reserva.getCliente().getCelular());
        dados.put("apartamento", reserva.getApartamento().getNumeroApartamento());
        dados.put("checkin", reserva.getDataCheckin().toString());
        dados.put("checkout", reserva.getDataCheckout().toString());
        dados.put("total", reserva.getTotalHospedagem());
        
        enviarEvento("NOVA_RESERVA", dados);
    }
    
    /**
     * 🏨 CHECK-IN
     */
    public void notificarCheckIn(Reserva reserva) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("reserva_id", reserva.getId());
        dados.put("cliente_nome", reserva.getCliente().getNome());
        dados.put("cliente_telefone", reserva.getCliente().getCelular());
        dados.put("apartamento", reserva.getApartamento().getNumeroApartamento());
        
        enviarEvento("CHECK_IN", dados);
    }
    
    /**
     * 🚪 CHECK-OUT
     */
    public void notificarCheckOut(Reserva reserva) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("reserva_id", reserva.getId());
        dados.put("cliente_nome", reserva.getCliente().getNome());
        dados.put("cliente_telefone", reserva.getCliente().getCelular());
        dados.put("apartamento", reserva.getApartamento().getNumeroApartamento());
        
        enviarEvento("CHECK_OUT", dados);
    }
}
