package com.divan.controller;

import com.divan.dto.PagamentoRequestDTO;
import com.divan.dto.ResumoPagamentosDTO;
import com.divan.entity.Apartamento;
import com.divan.entity.FechamentoCaixa;
import com.divan.entity.Pagamento;
import com.divan.entity.Reserva;
import com.divan.repository.ApartamentoRepository;
import com.divan.repository.FechamentoCaixaRepository;
import com.divan.repository.ReservaRepository;
import com.divan.service.PagamentoService;
import com.divan.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.divan.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @Autowired
    private FechamentoCaixaRepository caixaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    @PostMapping
    public ResponseEntity<?> processarPagamento(@Valid @RequestBody PagamentoRequestDTO dto) {
        try {
            // ✅ VERIFICAR CAIXA ABERTO
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            var usuarioOpt = usuarioRepository.findByUsername(login);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuário não encontrado"));
            }

            Long usuarioId = usuarioOpt.get().getId();
            boolean caixaAberto = caixaRepository
                .findByUsuarioIdAndStatus(usuarioId, FechamentoCaixa.StatusCaixa.ABERTO)
                .isPresent();

            if (!caixaAberto) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Caixa não aberto. Abra o caixa antes de registrar pagamentos."));
            }

            // Buscar reserva
            Optional<Reserva> reservaOpt = reservaService.buscarPorId(dto.getReservaId());
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Reserva não encontrada"));
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
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @PostMapping("/adiantamento")
    public ResponseEntity<?> processarAdiantamento(@Valid @RequestBody PagamentoRequestDTO dto) {
        try {
            // ✅ VERIFICAR CAIXA ABERTO (mesma lógica do pagamento normal)
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            var usuarioOpt = usuarioRepository.findByUsername(login);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuário não encontrado"));
            }

            Long usuarioId = usuarioOpt.get().getId();
            boolean caixaAberto = caixaRepository
                .findByUsuarioIdAndStatus(usuarioId, FechamentoCaixa.StatusCaixa.ABERTO)
                .isPresent();

            if (!caixaAberto) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Caixa não aberto. Abra o caixa antes de registrar adiantamentos."));
            }

            // Buscar reserva
            Optional<Reserva> reservaOpt = reservaService.buscarPorId(dto.getReservaId());
            if (reservaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Reserva não encontrada"));
            }

            // Criar pagamento (será marcado como ADIANTAMENTO no service)
            Pagamento pagamento = new Pagamento();
            pagamento.setReserva(reservaOpt.get());
            pagamento.setValor(dto.getValor());
            pagamento.setFormaPagamento(dto.getFormaPagamento());
            pagamento.setObservacao(dto.getObservacao());

            Pagamento adiantamentoProcessado = pagamentoService.processarAdiantamento(pagamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(adiantamentoProcessado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
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
    
    @PostMapping("/pre-reserva")
    public ResponseEntity<?> processarPagamentoPreReserva(@RequestBody Map<String, Object> body) {
        try {
            // ✅ VERIFICAR CAIXA ABERTO
            String login = SecurityContextHolder.getContext().getAuthentication().getName();
            var usuarioOpt = usuarioRepository.findByUsername(login);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuário não encontrado"));
            }

            Long usuarioId = usuarioOpt.get().getId();
            boolean caixaAberto = caixaRepository
                .findByUsuarioIdAndStatus(usuarioId, FechamentoCaixa.StatusCaixa.ABERTO)
                .isPresent();

            if (!caixaAberto) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Caixa não aberto. Abra o caixa antes de registrar pagamentos."));
            }

            Long reservaId = Long.parseLong(body.get("reservaId").toString());
            BigDecimal valor = new BigDecimal(body.get("valor").toString());
            String formaPagamentoStr = body.get("formaPagamento").toString();
            String observacao = body.containsKey("observacao") && body.get("observacao") != null
                ? body.get("observacao").toString() : null;

            Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            // ✅ ACEITA PAGAMENTO EM PRÉ-RESERVA E EM ATIVA
            if (reserva.getStatus() != Reserva.StatusReservaEnum.PRE_RESERVA
                    && reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Reserva não está em status válido para pagamento."));
            }

            // ✅ REGISTRAR PAGAMENTO
            Pagamento pagamento = new Pagamento();
            pagamento.setReserva(reserva);
            pagamento.setValor(valor);
            pagamento.setFormaPagamento(
                Pagamento.FormaPagamentoEnum.valueOf(formaPagamentoStr));
            pagamento.setObservacao(observacao);

            Pagamento pagamentoProcessado = pagamentoService.processarPagamento(pagamento);

            // ✅ SE ERA PRÉ-RESERVA E PAGOU TUDO — ATIVAR AUTOMATICAMENTE
            BigDecimal totalApagar = reserva.getTotalApagar();
            if (reserva.getStatus() == Reserva.StatusReservaEnum.PRE_RESERVA
                    && totalApagar != null
                    && totalApagar.compareTo(BigDecimal.ZERO) <= 0) {
                reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
                reserva.setDataCheckin(reserva.getDataCheckin().toLocalDate()
                    .atTime(java.time.LocalTime.now()));
                reserva.setDataCheckout(reserva.getDataCheckout().toLocalDate().atTime(12, 0));
                reservaRepository.save(reserva);

                Apartamento apartamento = reserva.getApartamento();
                apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
                apartamentoRepository.save(apartamento);

                return ResponseEntity.ok(Map.of(
                    "mensagem", "Pagamento registrado e reserva ATIVADA automaticamente!",
                    "ativada", true,
                    "pagamentoId", pagamentoProcessado.getId()
                ));
            }

            return ResponseEntity.ok(Map.of(
                "mensagem", "Pagamento registrado na pré-reserva!",
                "ativada", false,
                "pagamentoId", pagamentoProcessado.getId()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
