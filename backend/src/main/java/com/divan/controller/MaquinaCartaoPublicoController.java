package com.divan.controller;

import com.divan.service.MaquinaCartaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/publico/cartao")
public class MaquinaCartaoPublicoController {

    @Autowired
    private MaquinaCartaoService maquinaCartaoService;

    @PostMapping("/confirmacao")
    public ResponseEntity<?> confirmar(@RequestBody Map<String, Object> body) {
        try {
            String referencia = body.get("referencia") != null
                ? body.get("referencia").toString()
                : (body.get("correlation_id") != null ? body.get("correlation_id").toString() : null);

            if (referencia == null) {
                return ResponseEntity.ok(Map.of("erro", "referencia não informada"));
            }

            String status = body.get("status") != null ? body.get("status").toString() : null;
            boolean statusValido = status == null
                || status.equalsIgnoreCase("pago")
                || status.equalsIgnoreCase("acredited")
                || status.equalsIgnoreCase("accredited");

            if (!statusValido) {
                System.out.println("ℹ️ Cartão ignorado — status recebido: " + status + " (referencia: " + referencia + ")");
                return ResponseEntity.ok(Map.of("mensagem", "Status não é pago/acredited, ignorado"));
            }

            maquinaCartaoService.confirmarPagamento(referencia);
            return ResponseEntity.ok(Map.of("mensagem", "Confirmado"));

        } catch (Exception e) {
            System.err.println("⚠️ Erro ao confirmar pagamento de cartão: " + e.getMessage());
            return ResponseEntity.ok(Map.of("erro", e.getMessage()));
        }
    }
}
