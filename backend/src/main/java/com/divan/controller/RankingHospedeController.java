package com.divan.controller;


import com.divan.dto.RankingHospedeDTO;

import com.divan.service.RankingHospedeService;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;


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

     *

     * Parâmetros (todos opcionais):

     *   - dataInicio   → yyyy-MM-dd  (filtra check-in >= dataInicio)

     *   - dataFim      → yyyy-MM-dd  (filtra check-in <= dataFim)

     *   - ordenarPor   → "hospedagens" (padrão) | "diarias" | "gasto" | "diasSemHospedar"

     *

     * Ex.: /api/clientes/ranking?dataInicio=2026-01-01&dataFim=2026-06-30&ordenarPor=gasto

     */

    @GetMapping("/ranking")

    public ResponseEntity<List<RankingHospedeDTO>> ranking(

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,

            @RequestParam(required = false, defaultValue = "hospedagens") String ordenarPor) {

        return ResponseEntity.ok(service.ranking(dataInicio, dataFim, ordenarPor));

    }

}
