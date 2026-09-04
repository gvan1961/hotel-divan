package com.divan.controller;

import com.divan.entity.CobrancaPix;
import com.divan.service.PixService;
import com.divan.service.WhatsAppService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/pix")
@CrossOrigin(origins = "*")
public class PixController {

    @Autowired
    private PixService pixService;
    
    @Autowired
    private WhatsAppService whatsAppService;

    @PostMapping("/gerar")
    public ResponseEntity<?> gerar(@RequestBody Map<String, Object> body) {
        try {
            BigDecimal valor = new BigDecimal(body.get("valor").toString());
            String comentario = body.get("comentario") != null ? body.get("comentario").toString() : null;
            Long reservaId = body.get("reservaId") != null ? Long.parseLong(body.get("reservaId").toString()) : null;

            CobrancaPix cobranca = pixService.gerarCobranca(valor, comentario, reservaId);
            return ResponseEntity.ok(cobranca);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PostMapping("/enviar-whatsapp")
    public ResponseEntity<?> enviarPorWhatsApp(@RequestBody Map<String, Object> body) {
        try {
            String numero = body.get("numero").toString();
            String qrCodeImage = body.get("qrCodeImage").toString();
            String brCode = body.get("brCode").toString();
            BigDecimal valor = new BigDecimal(body.get("valor").toString());

            String legenda = "💰 Cobrança Pix — R$ " + valor.setScale(2, java.math.RoundingMode.HALF_UP) +
                "\n\nSe preferir copiar o código Pix:\n" + brCode;

            WhatsAppService.ResultadoEnvio resultado = whatsAppService.enviarImagem(numero, qrCodeImage, legenda);

            if (!resultado.isSucesso()) {
                return ResponseEntity.badRequest().body(Map.of("erro", resultado.getErro()));
            }
            return ResponseEntity.ok(Map.of("mensagem", "Enviado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
