package com.divan.controller;

import com.divan.dto.DiariaRequestDTO;
import com.divan.dto.DiariaResponseDTO;
import com.divan.entity.Diaria;
import com.divan.entity.TipoApartamento;
import com.divan.repository.TipoApartamentoRepository;
import com.divan.service.DiariaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diarias")
@CrossOrigin(origins = "*")
public class DiariaController {
    
    @Autowired
    private DiariaService diariaService;
    
    @Autowired
    private TipoApartamentoRepository tipoApartamentoRepository;
    
    @GetMapping("/tipo-apartamento/{tipoApartamentoId}")
    public ResponseEntity<List<Diaria>> buscarPorTipoApartamento(@PathVariable Long tipoApartamentoId) {
        try {
            List<Diaria> diarias = diariaService.buscarPorTipo(tipoApartamentoId);
            return ResponseEntity.ok(diarias);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody DiariaRequestDTO dto) {
        try {
            // Converter DTO para Entity
            Diaria diaria = new Diaria();
            diaria.setQuantidade(dto.getQuantidade());
            diaria.setValor(dto.getValor());
            
            Diaria diariaSalva = diariaService.salvar(diaria, dto.getTipoApartamentoId());
            
            // Converter para Response DTO
            DiariaResponseDTO response = converterParaResponseDTO(diariaSalva);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<DiariaResponseDTO>> listarTodas() {
        List<Diaria> diarias = diariaService.listarTodas();
        List<DiariaResponseDTO> response = diarias.stream()
            .map(this::converterParaResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Diaria> diaria = diariaService.buscarPorId(id);
        if (diaria.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DiariaResponseDTO response = converterParaResponseDTO(diaria.get());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tipo/{tipoApartamentoId}")
    public ResponseEntity<?> buscarPorTipo(@PathVariable Long tipoApartamentoId) {
        try {
            List<Diaria> diarias = diariaService.buscarPorTipo(tipoApartamentoId);
            List<DiariaResponseDTO> response = diarias.stream()
                .map(this::converterParaResponseDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/tipo/{tipoApartamentoId}/quantidade/{quantidade}")
    public ResponseEntity<?> buscarPorTipoEQuantidade(
            @PathVariable Long tipoApartamentoId,
            @PathVariable Integer quantidade) {
        try {
            Optional<Diaria> diaria = diariaService.buscarPorTipoEQuantidade(tipoApartamentoId, quantidade);
            if (diaria.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            DiariaResponseDTO response = converterParaResponseDTO(diaria.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody DiariaRequestDTO dto) {
        try {
            // Converter DTO para Entity
            Diaria diaria = new Diaria();
            diaria.setQuantidade(dto.getQuantidade());
            diaria.setValor(dto.getValor());
            
            Diaria diariaAtualizada = diariaService.atualizar(id, diaria, dto.getTipoApartamentoId());
            
            // Converter para Response DTO
            DiariaResponseDTO response = converterParaResponseDTO(diariaAtualizada);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            diariaService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Método auxiliar para converter Entity para DTO
    private DiariaResponseDTO converterParaResponseDTO(Diaria diaria) {
        DiariaResponseDTO dto = new DiariaResponseDTO();
        dto.setId(diaria.getId());
        dto.setTipoApartamentoId(diaria.getTipoApartamento().getId());
        dto.setTipoApartamento(diaria.getTipoApartamento().getTipo());
        dto.setDescricaoTipoApartamento(diaria.getTipoApartamento().getDescricao());
        dto.setQuantidade(diaria.getQuantidade());
        dto.setValor(diaria.getValor());
        return dto;
    }
}
