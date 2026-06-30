package com.divan.controller;

import com.divan.entity.VoucherWifi;
import com.divan.service.MikrotikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voucher-wifi")
public class VoucherWifiController {

    @Autowired
    private MikrotikService mikrotikService;

    /**
     * Gera vouchers de Wi-Fi para a reserva (padrão: 2 vouchers).
     */
    @PostMapping("/gerar/{reservaId}")
    public ResponseEntity<?> gerarVouchers(
            @PathVariable Long reservaId,
            @RequestParam(defaultValue = "2") int quantidade) {
        try {
            List<VoucherWifi> vouchers = mikrotikService.gerarESalvarVouchers(reservaId, quantidade);
            return ResponseEntity.ok(vouchers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Cancela todos os vouchers ativos de uma reserva (chamado no check-out).
     */
    @PostMapping("/cancelar/{reservaId}")
    public ResponseEntity<?> cancelarVouchers(@PathVariable Long reservaId) {
        try {
            mikrotikService.cancelarVouchersDaReserva(reservaId);
            return ResponseEntity.ok(Map.of("mensagem", "Vouchers cancelados com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Lista vouchers de uma reserva (ativos e cancelados).
     */
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> listarVouchers(@PathVariable Long reservaId) {
        try {
            List<VoucherWifi> vouchers = mikrotikService.listarVouchersDaReserva(reservaId);
            return ResponseEntity.ok(vouchers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
