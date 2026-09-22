package com.divan.controller;

import com.divan.service.PixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/publico/pix")
public class PixPublicoController {

    @Autowired
    private PixService pixService;

    @PostMapping("/confirmacao")
    public ResponseEntity<?> confirmar(@RequestBody Map<String, Object> body) {
        try {
            String correlationId = body.get("correlation_id") != null
                ? body.get("correlation_id").toString()
                : (body.get("correlationID") != null ? body.get("correlationID").toString() : null);

            if (correlationId == null) {
                return ResponseEntity.ok(Map.of("erro", "correlation_id não informado"));
            }

            String status = body.get("status") != null ? body.get("status").toString() : null;

            boolean pago = status != null
                && (status.equalsIgnoreCase("COMPLETED") || status.equalsIgnoreCase("PAGO"));
            boolean expirado = status != null && status.equalsIgnoreCase("EXPIRED");

            if (pago) {
                pixService.confirmarPagamento(correlationId);
                return ResponseEntity.ok(Map.of("mensagem", "Confirmado"));
            }

            if (expirado) {
                pixService.marcarExpirado(correlationId);
                return ResponseEntity.ok(Map.of("mensagem", "Marcado como expirado"));
            }

            System.out.println("ℹ️ Pix ignorado — status recebido: " + status + " (correlation_id: " + correlationId + ")");
            return ResponseEntity.ok(Map.of("mensagem", "Status não é COMPLETED/PAGO/EXPIRED, ignorado"));
            
            
            
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao confirmar Pix: " + e.getMessage());
            return ResponseEntity.ok(Map.of("erro", e.getMessage()));
        }
    }
}
