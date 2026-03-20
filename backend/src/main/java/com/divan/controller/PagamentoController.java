package com.divan.controller;

import com.divan.dto.PagamentoRequestDTO;
import com.divan.dto.ResumoPagamentosDTO;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import com.divan.service.PagamentoService;
import com.divan.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagamentos")
@CrossOrigin(origins = "*")
public class PagamentoController {
    
    @Autowired
    private PagamentoService pagamentoService;
    
    @Autowired
    private ReservaService reservaService;
    
    @PostMapping
    public ResponseEntity<?> processarPagamento(@Valid @RequestBody PagamentoRequestDTO dto) {
        try {
            // Buscar reserva
            Optional<Reserva> reservaOpt = reservaService.buscarPorId(dto.getReservaId());
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Reserva não encontrada");
            }
            
            // Criar pagamento
            Pagamento pagamento = new Pagamento();
            pagamento.setReserva(reservaOpt.get());
            pagamento.setValor(dto.getValor());
            pagamento.setFormaPagamento(dto.getFormaPagamento());
            pagamento.setObservacao(dto.getObservacao());
            
            Pagamento pagamentoProcessado = pagamentoService.processarPagamento(pagamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(pagamentoProcessado);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<Pagamento>> buscarPorReserva(@PathVariable Long reservaId) {
        List<Pagamento> pagamentos = pagamentoService.buscarPorReserva(reservaId);
        return ResponseEntity.ok(pagamentos);
    }
    
    @GetMapping("/do-dia")
    public ResponseEntity<List<Pagamento>> buscarPagamentosDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {
        List<Pagamento> pagamentos = pagamentoService.buscarPagamentosDoDia(data);
        return ResponseEntity.ok(pagamentos);
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<Pagamento>> buscarPagamentosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<Pagamento> pagamentos = pagamentoService.buscarPagamentosPorPeriodo(inicio, fim);
        return ResponseEntity.ok(pagamentos);
    }
    
    @GetMapping("/resumo-do-dia")
    public ResponseEntity<ResumoPagamentosDTO> gerarResumoDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime data) {
        ResumoPagamentosDTO resumo = pagamentoService.gerarResumoDoDia(data);
        return ResponseEntity.ok(resumo);
    }
}
