package com.divan.controller;

import com.divan.dto.ManutencaoRequestDTO;
import com.divan.dto.ManutencaoResponseDTO;
import com.divan.entity.TipoServico;
import com.divan.entity.StatusManutencao;
import com.divan.service.ManutencaoApartamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manutencoes")
public class ManutencaoApartamentoController {

    private final ManutencaoApartamentoService service;

    public ManutencaoApartamentoController(ManutencaoApartamentoService service) {
        this.service = service;
    }

    // ========== CRIAR ==========

    @PostMapping
    public ResponseEntity<ManutencaoResponseDTO> criar(@RequestBody ManutencaoRequestDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    // ========== BUSCAR POR ID ==========

    @GetMapping("/{id}")
    public ResponseEntity<ManutencaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // ========== HISTÓRICO DO APARTAMENTO ==========

    @GetMapping("/apartamento/{apartamentoId}")
    public ResponseEntity<List<ManutencaoResponseDTO>> historico(@PathVariable Long apartamentoId) {
        return ResponseEntity.ok(service.historicoDoApartamento(apartamentoId));
    }

    // ========== LISTAR COM FILTROS ==========

    @GetMapping
    public ResponseEntity<List<ManutencaoResponseDTO>> listar(
            @RequestParam(required = false) Long apartamentoId,
            @RequestParam(required = false) TipoServico tipoServico,
            @RequestParam(required = false) StatusManutencao status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.buscarComFiltros(apartamentoId, tipoServico, status, inicio, fim));
    }

    // ========== ATUALIZAR ==========

    @PutMapping("/{id}")
    public ResponseEntity<ManutencaoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody ManutencaoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    // ========== CONCLUIR ==========

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ManutencaoResponseDTO> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(service.concluir(id));
    }

    // ========== EXCLUIR ==========

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ========== FILTRO AR VENCIDO ==========

    @GetMapping("/apartamento/{apartamentoId}/filtro-ar-vencido")
    public ResponseEntity<Boolean> filtroArVencido(@PathVariable Long apartamentoId) {
        return ResponseEntity.ok(service.filtroArVencido(apartamentoId));
    }
}
