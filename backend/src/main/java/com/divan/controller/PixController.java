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
            Object itens = body.get("itens");

            CobrancaPix cobranca = pixService.gerarCobranca(valor, comentario, reservaId, itens);
            return ResponseEntity.ok(cobranca);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        return ResponseEntity.ok(pixService.listarAtivas());
    }
    
    @PostMapping("/enviar-whatsapp")
    public ResponseEntity<?> enviarPorWhatsApp(@RequestBody Map<String, Object> body) {
        try {
            String numero = body.get("numero").toString();
            String qrCodeImage = body.get("qrCodeImage").toString();
            String brCode = body.get("brCode").toString();
            BigDecimal valor = new BigDecimal(body.get("valor").toString());

            // 1ª mensagem: imagem do QR Code, com legenda simples
            String legendaImagem = "💰 Cobrança Pix — R$ " + valor.setScale(2, java.math.RoundingMode.HALF_UP)
                + "\n\nEscaneie o QR Code acima ou aguarde o código abaixo para copiar e colar.";

            WhatsAppService.ResultadoEnvio resultadoImagem = whatsAppService.enviarImagem(numero, qrCodeImage, legendaImagem);
            if (!resultadoImagem.isSucesso()) {
                return ResponseEntity.badRequest().body(Map.of("erro", resultadoImagem.getErro()));
            }

            // 2ª mensagem: só o código Pix puro, sem nada mais junto
            WhatsAppService.ResultadoEnvio resultadoTexto = whatsAppService.enviarTexto(numero, brCode);
            if (!resultadoTexto.isSucesso()) {
                return ResponseEntity.badRequest().body(Map.of("erro", resultadoTexto.getErro()));
            }

            return ResponseEntity.ok(Map.of("mensagem", "Enviado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            pixService.cancelarCobranca(id);
            return ResponseEntity.ok(Map.of("mensagem", "Cobrança cancelada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/status")
    public ResponseEntity<?> consultarStatus(@PathVariable Long id) {
        return ResponseEntity.ok(pixService.buscarPorId(id));
    }
    
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable Long id) {
        try {
            pixService.marcarConfirmado(id);
            return ResponseEntity.ok(Map.of("mensagem", "Confirmado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/reserva/{reservaId}/ativa")
    public ResponseEntity<?> buscarAtivaPorReserva(@PathVariable Long reservaId) {
        CobrancaPix cobranca = pixService.buscarAtivaPorReserva(reservaId);
        if (cobranca == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cobranca);
    }
    
    
}
