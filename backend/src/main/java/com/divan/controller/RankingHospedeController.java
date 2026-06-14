package com.divan.controller;

import com.divan.dto.RankingHospedeDTO;
import com.divan.service.RankingHospedeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class RankingHospedeController {

    private final RankingHospedeService service;

    public RankingHospedeController(RankingHospedeService service) {
        this.service = service;
    }

    /**
     * GET /api/clientes/ranking
     * Parâmetros opcionais: inicio e fim (yyyy-MM-dd)
     * Ex.: /api/clientes/ranking?inicio=2026-01-01&fim=2026-06-30
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<RankingHospedeDTO>> ranking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.ranking(inicio, fim));
    }
}
