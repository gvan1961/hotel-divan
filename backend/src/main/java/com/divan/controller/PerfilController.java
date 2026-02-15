package com.divan.controller;

import com.divan.dto.PerfilRequestDTO;
import com.divan.dto.PerfilResponseDTO;
import com.divan.entity.Perfil;
import com.divan.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfis")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class PerfilController {
    
    @Autowired
    private PerfilService perfilService;
    
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody PerfilRequestDTO dto) {
        try {
            Perfil perfil = perfilService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(perfil);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<PerfilResponseDTO>> listarTodos() {
        List<PerfilResponseDTO> perfis = perfilService.listarTodos();
        return ResponseEntity.ok(perfis);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            PerfilResponseDTO perfil = perfilService.buscarPorId(id);
            return ResponseEntity.ok(perfil);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequestDTO dto) {
        try {
            Perfil perfil = perfilService.atualizar(id, dto);
            return ResponseEntity.ok(perfil);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            perfilService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
