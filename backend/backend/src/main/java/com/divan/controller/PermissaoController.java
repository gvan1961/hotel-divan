package com.divan.controller;

import com.divan.dto.PermissaoRequestDTO;
import com.divan.dto.PermissaoResponseDTO;
import com.divan.entity.Permissao;
import com.divan.service.PermissaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissoes")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class PermissaoController {
    
    @Autowired
    private PermissaoService permissaoService;
    
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody PermissaoRequestDTO dto) {
        try {
            Permissao permissao = permissaoService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(permissao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<PermissaoResponseDTO>> listarTodas() {
        List<PermissaoResponseDTO> permissoes = permissaoService.listarTodas();
        return ResponseEntity.ok(permissoes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            PermissaoResponseDTO permissao = permissaoService.buscarPorId(id);
            return ResponseEntity.ok(permissao);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<PermissaoResponseDTO>> listarPorCategoria(@PathVariable String categoria) {
        List<PermissaoResponseDTO> permissoes = permissaoService.listarPorCategoria(categoria);
        return ResponseEntity.ok(permissoes);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody PermissaoRequestDTO dto) {
        try {
            Permissao permissao = permissaoService.atualizar(id, dto);
            return ResponseEntity.ok(permissao);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            permissaoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
