package com.divan.controller;

import com.divan.dto.RelatorioFechamentoCaixaDTO;
import com.divan.service.RelatorioFechamentoCaixaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/relatorio-fechamento-caixa")
@CrossOrigin(origins = "*")
public class RelatorioFechamentoCaixaController {
    
    @Autowired
    private RelatorioFechamentoCaixaService relatorioService;
    
    /**
     * 📊 GERAR RELATÓRIO COMPLETO DO FECHAMENTO
     */
    @GetMapping("/{caixaId}")
    public ResponseEntity<?> gerarRelatorioCompleto(@PathVariable Long caixaId) {
        try {
            RelatorioFechamentoCaixaDTO relatorio = relatorioService.gerarRelatorioCompleto(caixaId);
            return ResponseEntity.ok(relatorio);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
