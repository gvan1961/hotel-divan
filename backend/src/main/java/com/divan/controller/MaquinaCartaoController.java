package com.divan.controller;

import com.divan.entity.CobrancaCartao;
import com.divan.repository.ReservaRepository;
import com.divan.service.MaquinaCartaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cartao")
@CrossOrigin(origins = "*")
public class MaquinaCartaoController {

    @Autowired
    private MaquinaCartaoService maquinaCartaoService;
    
    @Autowired
    private ReservaRepository reservaRepository;

    @PostMapping("/gerar")
    public ResponseEntity<?> gerar(@RequestBody Map<String, Object> body) {
        try {
            BigDecimal valor = new BigDecimal(body.get("valor").toString());
            String formaPagamento = body.get("formaPagamento").toString();
            Long reservaId = body.get("reservaId") != null
                ? Long.parseLong(body.get("reservaId").toString()) : null;
            Object itens = body.get("itens");

            CobrancaCartao cobranca = maquinaCartaoService.gerarCobranca(valor, formaPagamento, reservaId, itens);
            return ResponseEntity.ok(cobranca);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> consultarStatus(@PathVariable Long id) {
        return ResponseEntity.ok(maquinaCartaoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            maquinaCartaoService.cancelarCobranca(id);
            return ResponseEntity.ok(Map.of("mensagem", "Cancelado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        List<CobrancaCartao> cobrancas = maquinaCartaoService.listarAtivas();

        List<Map<String, Object>> resultado = cobrancas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("correlationId", c.getCorrelationId());
            map.put("valor", c.getValor());
            map.put("formaPagamento", c.getFormaPagamento());
            map.put("ordemMercadoPago", c.getOrdemMercadoPago());
            map.put("reservaId", c.getReservaId());
            map.put("status", c.getStatus());
            map.put("dataCriacao", c.getDataCriacao());

            if (c.getReservaId() != null) {
                reservaRepository.findById(c.getReservaId()).ifPresent(reserva -> {
                    if (reserva.getApartamento() != null) {
                        map.put("numeroApartamento", reserva.getApartamento().getNumeroApartamento());
                    }
                });
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }
    
    @GetMapping("/historico")
    public ResponseEntity<?> buscarHistorico(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataInicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate dataFim,
            @RequestParam(required = false) Long reservaId,
            @RequestParam(required = false) String status) {

        java.time.LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        java.time.LocalDateTime fim = dataFim != null ? dataFim.atTime(23, 59, 59) : null;

        List<CobrancaCartao> cobrancas = maquinaCartaoService.buscarHistorico(inicio, fim, reservaId, status);

        List<Map<String, Object>> resultado = cobrancas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("correlationId", c.getCorrelationId());
            map.put("valor", c.getValor());
            map.put("formaPagamento", c.getFormaPagamento());
            map.put("ordemMercadoPago", c.getOrdemMercadoPago());
            map.put("reservaId", c.getReservaId());
            map.put("status", c.getStatus());
            map.put("dataCriacao", c.getDataCriacao());

            if (c.getReservaId() != null) {
                reservaRepository.findById(c.getReservaId()).ifPresent(reserva -> {
                    if (reserva.getApartamento() != null) {
                        map.put("numeroApartamento", reserva.getApartamento().getNumeroApartamento());
                    }
                });
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }
}
