package com.divan.controller;

import com.divan.dto.PagamentoRequestDTO;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import com.divan.service.PagamentoService;
import com.divan.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagamentos")
@CrossOrigin(origins = "*")
public class PagamentoController {
    
    @Autowired
    private PagamentoService pagamentoService;
    
    @Autowired
    private ReservaService reservaService;
    
    /**
     * ✅ PROCESSAR PAGAMENTO (COM VALIDAÇÃO DE CAIXA ABERTO)
     */
    @PostMapping
    public ResponseEntity<?> processarPagamento(
            @Valid @RequestBody PagamentoRequestDTO dto,
            @RequestParam Long usuarioId
    ) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("💰 PROCESSANDO PAGAMENTO");
            System.out.println("   Reserva ID: " + dto.getReservaId());
            System.out.println("   Valor: " + dto.getValor());
            System.out.println("   Forma: " + dto.getFormaPagamento());
            System.out.println("   Usuário ID: " + usuarioId);
            System.out.println("═══════════════════════════════════════════");

            // Buscar reserva
            Optional<Reserva> reservaOpt = reservaService.buscarPorId(dto.getReservaId());
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "erro", "Reserva não encontrada"
                ));
            }

            // Criar pagamento
            Pagamento pagamento = new Pagamento();
            pagamento.setReserva(reservaOpt.get());
            pagamento.setValor(dto.getValor());
            pagamento.setFormaPagamento(dto.getFormaPagamento());
            pagamento.setDescricao(dto.getObservacao());
            
            // ✅ DEFINIR O TIPO COMO HOSPEDAGEM (campo obrigatório)
            pagamento.setTipo("HOSPEDAGEM");

            // Processar pagamento (valida caixa aberto)
            Pagamento pagamentoProcessado = pagamentoService.processarPagamento(pagamento, usuarioId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "sucesso", true,
                "mensagem", "Pagamento processado com sucesso!",
                "pagamento", pagamentoProcessado
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Erro: " + e.getMessage());

            // Tratamento específico para caixa fechado
            if (e.getMessage() != null && e.getMessage().contains("CAIXA FECHADO")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "sucesso", false,
                    "erro", e.getMessage(),
                    "tipo", "CAIXA_FECHADO"
                ));
            }

            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "sucesso", false,
                "erro", "Erro interno ao processar pagamento: " + e.getMessage()
            ));
        }
    }

    /**
     * ✅ BUSCAR PAGAMENTOS POR RESERVA
     */
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> buscarPorReserva(@PathVariable Long reservaId) {
        try {
            var pagamentos = pagamentoService.buscarPorReserva(reservaId);
            return ResponseEntity.ok(pagamentos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    /**
     * ✅ BUSCAR PAGAMENTOS DO DIA
     */
    @GetMapping("/dia")
    public ResponseEntity<?> buscarPagamentosDoDia() {
        try {
            // ✅ CORRIGIDO: Usar LocalDate ao invés de LocalDateTime
            var pagamentos = pagamentoService.buscarPagamentosDoDia(LocalDate.now());
            return ResponseEntity.ok(pagamentos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
