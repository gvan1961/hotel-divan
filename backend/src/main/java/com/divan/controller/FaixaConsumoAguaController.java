package com.divan.controller;

import com.divan.dto.FaixaConsumoAguaRequestDTO;
import com.divan.dto.FaixaConsumoAguaResponseDTO;
import com.divan.service.ControleConsumoAguaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/faixas-consumo-agua")
@CrossOrigin(origins = "*")
public class FaixaConsumoAguaController {

    @Autowired
    private ControleConsumoAguaService controleConsumoAguaService;

    @GetMapping
    public ResponseEntity<List<FaixaConsumoAguaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(controleConsumoAguaService.listarTodas());
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<FaixaConsumoAguaResponseDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(controleConsumoAguaService.listarPorEmpresa(empresaId));
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody FaixaConsumoAguaRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(controleConsumoAguaService.criar(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody FaixaConsumoAguaRequestDTO dto) {
        try {
            return ResponseEntity.ok(controleConsumoAguaService.atualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            controleConsumoAguaService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}