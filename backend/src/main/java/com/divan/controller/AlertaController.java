package com.divan.controller;

import com.divan.dto.AlertaDTO;
import com.divan.service.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@CrossOrigin(origins = "*")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    /**
     * 📊 BUSCAR TODOS OS ALERTAS AGRUPADOS
     * GET /api/alertas/todos-alertas
     */
    @GetMapping("/todos-alertas")
    public ResponseEntity<Map<String, List<AlertaDTO>>> buscarTodosAlertas() {
        try {
            Map<String, List<AlertaDTO>> alertas = alertaService.buscarTodosAlertas();
            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🚨 BUSCAR APENAS CONFLITOS
     * GET /api/alertas/conflitos
     */
    @GetMapping("/conflitos")
    public ResponseEntity<List<AlertaDTO>> buscarConflitos() {
        try {
            List<AlertaDTO> conflitos = alertaService.buscarConflitos();
            return ResponseEntity.ok(conflitos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ⏰ BUSCAR APENAS CHECKOUTS VENCIDOS
     * GET /api/alertas/checkouts-vencidos
     */
    @GetMapping("/checkouts-vencidos")
    public ResponseEntity<List<AlertaDTO>> buscarCheckoutsVencidos() {
        try {
            List<AlertaDTO> checkouts = alertaService.buscarCheckoutsVencidos();
            return ResponseEntity.ok(checkouts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔴 BUSCAR APENAS NO-SHOWS
     * GET /api/alertas/no-shows
     */
    @GetMapping("/no-shows")
    public ResponseEntity<List<AlertaDTO>> buscarNoShows() {
        try {
            List<AlertaDTO> noShows = alertaService.buscarNoShows();
            return ResponseEntity.ok(noShows);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ⚠️ BUSCAR PRÉ-RESERVAS EM RISCO
     * GET /api/alertas/pre-reservas-em-risco
     */
    @GetMapping("/pre-reservas-em-risco")
    public ResponseEntity<List<AlertaDTO>> buscarPreReservasEmRisco() {
        try {
            List<AlertaDTO> preReservas = alertaService.buscarPreReservasEmRisco();
            return ResponseEntity.ok(preReservas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
