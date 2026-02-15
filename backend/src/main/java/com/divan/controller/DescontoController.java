package com.divan.controller;

import com.divan.dto.DescontoRequestDTO;
import com.divan.entity.DescontoReserva;
import com.divan.service.DescontoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/descontos")
@CrossOrigin(origins = "*")
public class DescontoController {
    
    @Autowired
    private DescontoService descontoService;
    
    /**
     * ✅ APLICAR DESCONTO NA RESERVA
     */
    @PostMapping
    public ResponseEntity<?> aplicarDesconto(@RequestBody DescontoRequestDTO request) {
        try {
            DescontoReserva desconto = descontoService.aplicarDesconto(request);
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Desconto de R$ " + desconto.getValor() + " aplicado com sucesso!",
                "desconto", desconto
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    /**
     * ✅ LISTAR DESCONTOS DE UMA RESERVA
     */
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> listarDescontosPorReserva(@PathVariable Long reservaId) {
        try {
            List<DescontoReserva> descontos = descontoService.listarDescontosPorReserva(reservaId);
            return ResponseEntity.ok(descontos);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * ✅ REMOVER DESCONTO
     */
    @DeleteMapping("/{descontoId}")
    public ResponseEntity<?> removerDesconto(
            @PathVariable Long descontoId,
            @RequestParam Long usuarioId
    ) {
        try {
            descontoService.removerDesconto(descontoId, usuarioId);
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Desconto removido com sucesso!"
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
}