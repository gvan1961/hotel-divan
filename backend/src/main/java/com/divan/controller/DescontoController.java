package com.divan.controller;

import com.divan.entity.Desconto;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.Reserva;
import com.divan.entity.Usuario;
import com.divan.repository.DescontoRepository;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.ReservaRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/descontos")
@CrossOrigin(origins = "*")
public class DescontoController {

    @Autowired private DescontoRepository descontoRepository;
    @Autowired private ReservaRepository reservaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ExtratoReservaRepository extratoReservaRepository;

    @PostMapping
    public ResponseEntity<?> aplicarDesconto(@RequestBody Map<String, Object> body) {
        try {
            Long reservaId  = Long.parseLong(body.get("reservaId").toString());
            BigDecimal valor = new BigDecimal(body.get("valor").toString());
            String motivo   = body.getOrDefault("motivo", "Desconto aplicado").toString();
            Long usuarioId  = body.get("usuarioId") != null
                ? Long.parseLong(body.get("usuarioId").toString()) : null;

            Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

            // ✅ SALVAR DESCONTO
            Desconto desconto = new Desconto();
            desconto.setReserva(reserva);
            desconto.setValor(valor);
            desconto.setMotivo(motivo);
            desconto.setDataHoraDesconto(LocalDateTime.now());
            if (usuarioId != null) {
                usuarioRepository.findById(usuarioId)
                    .ifPresent(desconto::setUsuario);
            }
            descontoRepository.save(desconto);

            // ✅ ATUALIZAR TOTAIS DA RESERVA
            BigDecimal descontoAtual = reserva.getDesconto() != null
                ? reserva.getDesconto() : BigDecimal.ZERO;
            reserva.setDesconto(descontoAtual.add(valor));
            reserva.setTotalApagar(reserva.getTotalApagar().subtract(valor));
            reservaRepository.save(reserva);

            // ✅ LANÇAR NO EXTRATO
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setDescricao("Desconto: " + motivo);
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
            extrato.setQuantidade(1);
            extrato.setValorUnitario(valor.negate());
            extrato.setTotalLancamento(valor.negate());
            extrato.setDataHoraLancamento(LocalDateTime.now());
            extratoReservaRepository.save(extrato);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Desconto aplicado com sucesso",
                "descontoId", desconto.getId(),
                "novoTotalApagar", reserva.getTotalApagar()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> listarPorReserva(@PathVariable Long reservaId) {
        List<Desconto> descontos = descontoRepository.findByReservaId(reservaId);
        return ResponseEntity.ok(descontos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerDesconto(
            @PathVariable Long id,
            @RequestParam(required = false) Long usuarioId) {
        try {
            Desconto desconto = descontoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Desconto não encontrado"));

            Reserva reserva = desconto.getReserva();
            BigDecimal valor = desconto.getValor();

            // ✅ REVERTER TOTAIS
            BigDecimal descontoAtual = reserva.getDesconto() != null
                ? reserva.getDesconto() : BigDecimal.ZERO;
            reserva.setDesconto(descontoAtual.subtract(valor));
            reserva.setTotalApagar(reserva.getTotalApagar().add(valor));
            reservaRepository.save(reserva);

            // ✅ LANÇAR ESTORNO NO EXTRATO
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setDescricao("Estorno de desconto: " + desconto.getMotivo());
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
            extrato.setQuantidade(1);
            extrato.setValorUnitario(valor);
            extrato.setTotalLancamento(valor);
            extrato.setDataHoraLancamento(LocalDateTime.now());
            extratoReservaRepository.save(extrato);

            descontoRepository.delete(desconto);

            return ResponseEntity.ok(Map.of(
                "mensagem", "Desconto removido com sucesso",
                "valorEstornado", valor
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
