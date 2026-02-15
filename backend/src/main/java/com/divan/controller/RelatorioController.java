package com.divan.controller;

import com.divan.dto.*;
import com.divan.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioController {
    
    @Autowired
    private RelatorioService relatorioService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> gerarDashboard() {
        DashboardDTO dashboard = relatorioService.gerarDashboard();
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/ocupacao")
    public ResponseEntity<RelatorioOcupacaoDTO> gerarRelatorioOcupacao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        RelatorioOcupacaoDTO relatorio = relatorioService.gerarRelatorioOcupacao(data);
        return ResponseEntity.ok(relatorio);
    }
    
    @GetMapping("/faturamento")
    public ResponseEntity<RelatorioFaturamentoDTO> gerarRelatorioFaturamento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        RelatorioFaturamentoDTO relatorio = relatorioService.gerarRelatorioFaturamento(dataInicio, dataFim);
        return ResponseEntity.ok(relatorio);
    }
    
    @GetMapping("/produtos")
    public ResponseEntity<List<RelatorioProdutoDTO>> gerarRelatorioProdutos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<RelatorioProdutoDTO> relatorio = relatorioService.gerarRelatorioProdutos(dataInicio, dataFim);
        return ResponseEntity.ok(relatorio);
    }
    
    @GetMapping("/apartamentos")
    public ResponseEntity<List<RelatorioApartamentoDTO>> gerarRelatorioApartamentos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        List<RelatorioApartamentoDTO> relatorio = relatorioService.gerarRelatorioApartamentos(dataInicio, dataFim);
        return ResponseEntity.ok(relatorio);
    }
}
