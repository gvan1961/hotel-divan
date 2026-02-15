package com.divan.controller;

import com.divan.service.MakeWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestWebhookController {
    
    @Autowired
    private MakeWebhookService makeWebhookService;
    
    /**
     * 🧪 ENDPOINT DE TESTE
     * Acesse: http://localhost:8080/api/test/webhook
     */
    @GetMapping("/webhook")
    public ResponseEntity<Map<String, Object>> testarWebhook() {
        System.out.println("🧪 TESTANDO WEBHOOK...");
        
        // Criar dados de teste
        Map<String, Object> dadosTeste = new HashMap<>();
        dadosTeste.put("reserva_id", 999);
        dadosTeste.put("cliente_nome", "João Teste");
        dadosTeste.put("cliente_telefone", "(88) 99999-9999");
        dadosTeste.put("apartamento", "101");
        dadosTeste.put("mensagem", "Teste de integração com Make.com");
        
        // Enviar para Make.com
        makeWebhookService.enviarEvento("TESTE", dadosTeste);
        
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("sucesso", true);
        resposta.put("mensagem", "Evento de teste enviado para Make.com");
        resposta.put("dados", dadosTeste);
        
        return ResponseEntity.ok(resposta);
    }
}
