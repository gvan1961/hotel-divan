package com.divan.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.divan.dto.ResumoClienteDTO;
import com.divan.service.HistoricoClienteService;

@RestController
@RequestMapping("/api/clientes")
public class HistoricoClienteController {

    private final HistoricoClienteService service;

    public HistoricoClienteController(HistoricoClienteService service) {
        this.service = service;
    }

    /**
     * GET /api/clientes/{id}/historico
     * Retorna o resumo completo com estatísticas + lista de hospedagens.
     */
    @GetMapping("/{id}/historico")
    public ResponseEntity<ResumoClienteDTO> historico(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarHistorico(id));
    }
}
