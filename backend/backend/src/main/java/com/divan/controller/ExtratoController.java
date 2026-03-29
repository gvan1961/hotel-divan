package com.divan.controller;

import com.divan.dto.ExtratoDetalhadoDTO;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.HistoricoHospede;
import com.divan.entity.Reserva;
import com.divan.service.ExtratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extratos")
@CrossOrigin(origins = "*")
public class ExtratoController {
    
    @Autowired
    private ExtratoService extratoService;
    
    @GetMapping("/reserva/{reservaId}/detalhado")
    public ResponseEntity<ExtratoDetalhadoDTO> gerarExtratoDetalhado(@PathVariable Long reservaId) {
        try {
            ExtratoDetalhadoDTO extrato = extratoService.gerarExtratoDetalhado(reservaId);
            return ResponseEntity.ok(extrato);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<ExtratoReserva>> buscarExtratosPorReserva(@PathVariable Long reservaId) {
        try {
            List<ExtratoReserva> extratos = extratoService.buscarExtratosPorReserva(reservaId);
            return ResponseEntity.ok(extratos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/reserva/{reservaId}/historico")
    public ResponseEntity<List<HistoricoHospede>> buscarHistoricoPorReserva(@PathVariable Long reservaId) {
        try {
            List<HistoricoHospede> historico = extratoService.buscarHistoricoPorReserva(reservaId);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/apartamento/{numeroApartamento}")
    public ResponseEntity<List<Reserva>> buscarReservasPorApartamento(@PathVariable String numeroApartamento) {
        try {
            List<Reserva> reservas = extratoService.buscarReservasPorApartamento(numeroApartamento);
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
