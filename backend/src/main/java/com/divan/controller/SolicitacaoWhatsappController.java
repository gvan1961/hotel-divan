package com.divan.controller;

import com.divan.entity.SolicitacaoReservaWhatsapp;
import com.divan.repository.SolicitacaoReservaWhatsappRepository;
import com.divan.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/solicitacoes-whatsapp")
public class SolicitacaoWhatsappController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private SolicitacaoReservaWhatsappRepository repository;
    @Autowired
    private WhatsAppService whatsAppService;

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        List<SolicitacaoReservaWhatsapp> pendentes = repository
            .findByStatusOrderByDataSolicitacaoDesc(SolicitacaoReservaWhatsapp.StatusSolicitacao.PENDENTE);
        List<Map<String, Object>> resultado = pendentes.stream().map(s -> Map.<String, Object>of(
            "id", s.getId(),
            "nome", s.getNome(),
            "cpf", s.getCpf() != null ? s.getCpf() : "",
            "numeroWhatsapp", s.getNumeroWhatsapp(),
            "dataCheckin", s.getDataCheckin().format(FMT),
            "dataCheckout", s.getDataCheckout().format(FMT),
            "quantidadeHospedes", s.getQuantidadeHospedes(),
            "dataSolicitacao", s.getDataSolicitacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            "status", s.getStatus().name()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/pendentes/count")
    public ResponseEntity<?> contarPendentes() {
        long total = repository.countByStatus(SolicitacaoReservaWhatsapp.StatusSolicitacao.PENDENTE);
        return ResponseEntity.ok(Map.of("total", total));
    }

    @PatchMapping("/{id}/visualizar")
    public ResponseEntity<?> visualizar(@PathVariable Long id) {
        return repository.findById(id).map(s -> {
            s.setStatus(SolicitacaoReservaWhatsapp.StatusSolicitacao.VISUALIZADA);
            s.setDataVisualizacao(LocalDateTime.now());
            repository.save(s);
            return ResponseEntity.ok(Map.of("mensagem", "Marcada como visualizada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/atender")
    public ResponseEntity<?> atender(@PathVariable Long id) {
        return repository.findById(id).map(s -> {
            s.setStatus(SolicitacaoReservaWhatsapp.StatusSolicitacao.ATENDIDA);
            repository.save(s);
            return ResponseEntity.ok(Map.of("mensagem", "Marcada como atendida"));
        }).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/{id}/responder")
    public ResponseEntity<?> responder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return repository.findById(id).map(s -> {
            String mensagem = body.get("mensagem");
            if (mensagem == null || mensagem.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Mensagem nao informada"));
            }
            whatsAppService.enviarTexto(s.getNumeroWhatsapp(),
                "🏨 *Hotel Di Van*\n\n" + mensagem);
            return ResponseEntity.ok(Map.of("mensagem", "Resposta enviada com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> listarTodas() {
        List<SolicitacaoReservaWhatsapp> todas = repository.findAll();
        List<Map<String, Object>> resultado = todas.stream().map(s -> Map.<String, Object>of(
            "id", s.getId(),
            "nome", s.getNome(),
            "cpf", s.getCpf() != null ? s.getCpf() : "",
            "numeroWhatsapp", s.getNumeroWhatsapp(),
            "dataCheckin", s.getDataCheckin().format(FMT),
            "dataCheckout", s.getDataCheckout().format(FMT),
            "quantidadeHospedes", s.getQuantidadeHospedes(),
            "dataSolicitacao", s.getDataSolicitacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            "status", s.getStatus().name()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(resultado);
    }
}

