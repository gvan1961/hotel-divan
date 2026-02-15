package com.divan.controller;

import com.divan.entity.NotaVenda;
import com.divan.repository.NotaVendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notas-venda")
@CrossOrigin(origins = "*")
public class NotaVendaController {

    @Autowired
    private NotaVendaRepository notaVendaRepository;

    @GetMapping
    public ResponseEntity<List<NotaVenda>> listar() {
        List<NotaVenda> notas = notaVendaRepository.findAll();
        return ResponseEntity.ok(notas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<NotaVenda> nota = notaVendaRepository.findById(id);
        return nota.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<List<NotaVenda>> listarPorReserva(@PathVariable Long reservaId) {
        List<NotaVenda> notas = notaVendaRepository.findByReservaId(reservaId);
        return ResponseEntity.ok(notas);
    }

    @PatchMapping("/{id}/fechar")
    public ResponseEntity<?> fechar(@PathVariable Long id) {
        try {
            Optional<NotaVenda> notaOpt = notaVendaRepository.findById(id);
            if (notaOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Nota de venda não encontrada"));
            }

            NotaVenda nota = notaOpt.get();

            // ✅ CORRIGIDO: Status ao invés de StatusEnum
            if (nota.getStatus() == NotaVenda.Status.FECHADA) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Nota já está fechada"));
            }

            if (nota.getStatus() == NotaVenda.Status.CANCELADA) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Nota está cancelada"));
            }

            nota.setStatus(NotaVenda.Status.FECHADA);
            NotaVenda notaSalva = notaVendaRepository.save(nota);

            return ResponseEntity.ok(notaSalva);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @RequestParam(required = false) String motivo) {
        try {
            Optional<NotaVenda> notaOpt = notaVendaRepository.findById(id);
            if (notaOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Nota de venda não encontrada"));
            }

            NotaVenda nota = notaOpt.get();

            // ✅ CORRIGIDO
            if (nota.getStatus() == NotaVenda.Status.FECHADA) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Não é possível cancelar nota fechada"));
            }

            nota.setStatus(NotaVenda.Status.CANCELADA);
            
            // ✅ CORRIGIDO: Verificar se o campo observacao existe na entidade
            // Se não existir, remova estas linhas ou adicione o campo na entidade
            if (motivo != null && !motivo.trim().isEmpty()) {
                String obsAtual = nota.getObservacao() != null ? nota.getObservacao() : "";
                nota.setObservacao(obsAtual + (obsAtual.isEmpty() ? "" : " | ") + "CANCELADA: " + motivo);
            }

            NotaVenda notaSalva = notaVendaRepository.save(nota);

            return ResponseEntity.ok(notaSalva);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", e.getMessage()));
        }
    }
}
