package com.divan.controller;

import com.divan.dto.TipoApartamentoResponseDTO;
import com.divan.entity.TipoApartamento;
import com.divan.service.TipoApartamentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tipos-apartamento")
@CrossOrigin(origins = "*")
public class TipoApartamentoController {
    
    @Autowired
    private TipoApartamentoService tipoApartamentoService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('APARTAMENTO_CREATE', 'ROLE_ADMIN')")
    public ResponseEntity<?> criar(@Valid @RequestBody TipoApartamento tipoApartamento) {
        try {
            TipoApartamento tipoSalvo = tipoApartamentoService.salvar(tipoApartamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(tipoSalvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
   @GetMapping
    @PreAuthorize("hasAnyAuthority('APARTAMENTO_READ', 'ROLE_ADMIN')")
    public ResponseEntity<List<TipoApartamento>> listarTodos() {
        List<TipoApartamento> tipos = tipoApartamentoService.listarTodos();
        return ResponseEntity.ok(tipos);
    }
    
 //   @GetMapping
 //   public ResponseEntity<List<TipoApartamentoResponseDTO>> listarTodos() {
 //       List<TipoApartamentoResponseDTO> tipos = tipoApartamentoService.listarTodos();
 //       return ResponseEntity.ok(tipos);
 //   }
    
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('APARTAMENTO_READ', 'ROLE_ADMIN')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<TipoApartamento> tipo = tipoApartamentoService.buscarPorId(id);
        if (tipo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tipo.get());
    }
    
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyAuthority('APARTAMENTO_READ', 'ROLE_ADMIN')")
    public ResponseEntity<?> buscarPorTipo(@PathVariable TipoApartamento.TipoEnum tipo) {
        Optional<TipoApartamento> tipoApartamento = tipoApartamentoService.buscarPorTipo(tipo);
        if (tipoApartamento.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tipoApartamento.get());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('APARTAMENTO_UPDATE', 'ROLE_ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody TipoApartamento tipoApartamento) {
        try {
            TipoApartamento tipoAtualizado = tipoApartamentoService.atualizar(id, tipoApartamento);
            return ResponseEntity.ok(tipoAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('APARTAMENTO_DELETE', 'ROLE_ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            tipoApartamentoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}