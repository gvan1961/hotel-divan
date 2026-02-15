package com.divan.controller;

import com.divan.dto.UsuarioRequestDTO;
import com.divan.dto.UsuarioResponseDTO;
import com.divan.dto.AlterarSenhaDTO;
import com.divan.entity.Usuario;
import com.divan.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * 📋 LISTAR TODOS OS USUÁRIOS
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📋 LISTANDO TODOS OS USUÁRIOS");
        
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        
        System.out.println("✅ Total: " + usuarios.size() + " usuário(s)");
        System.out.println("═══════════════════════════════════════════");
        
        return ResponseEntity.ok(usuarios);
    }
    
    /**
     * 🔍 BUSCAR USUÁRIO POR ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        System.out.println("🔍 Buscando usuário ID: " + id);
        
        UsuarioResponseDTO usuario = usuarioService.buscarPorIdDTO(id);
        
        return ResponseEntity.ok(usuario);
    }
    
    /**
     * ➕ CRIAR NOVO USUÁRIO
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criar(@RequestBody UsuarioRequestDTO dto) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("➕ CRIANDO NOVO USUÁRIO");
            System.out.println("   Nome: " + dto.getNome());
            System.out.println("   Username: " + dto.getUsername());
            System.out.println("   Email: " + dto.getEmail());
            
            Usuario usuario = usuarioService.criar(dto);
            
            System.out.println("✅ Usuário criado com sucesso! ID: " + usuario.getId());
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "sucesso", true,
                "mensagem", "Usuário criado com sucesso!",
                "id", usuario.getId()
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    /**
     * ✏️ ATUALIZAR USUÁRIO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        try {
            System.out.println("═══════════════════════════════════════════");
            System.out.println("✏️ ATUALIZANDO USUÁRIO ID: " + id);
            
            Usuario usuario = usuarioService.atualizar(id, dto);
            
            System.out.println("✅ Usuário atualizado com sucesso!");
            System.out.println("═══════════════════════════════════════════");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Usuário atualizado com sucesso!"
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    /**
     * 🔐 ALTERAR SENHA
     */
    @PutMapping("/{id}/senha")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> alterarSenha(@PathVariable Long id, @RequestBody AlterarSenhaDTO dto) {
        try {
            System.out.println("🔐 Alterando senha do usuário ID: " + id);
            
            usuarioService.alterarSenha(id, dto);
            
            System.out.println("✅ Senha alterada com sucesso!");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Senha alterada com sucesso!"
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
    
    /**
     * 🔄 ATIVAR/DESATIVAR USUÁRIO
     */
    @PatchMapping("/{id}/ativo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> ativarDesativar(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        try {
            Boolean ativo = body.get("ativo");
            
            System.out.println("🔄 " + (ativo ? "Ativando" : "Desativando") + " usuário ID: " + id);
            
            usuarioService.ativarDesativar(id, ativo);
            
            System.out.println("✅ Usuário " + (ativo ? "ativado" : "desativado") + " com sucesso!");
            
            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "mensagem", "Usuário " + (ativo ? "ativado" : "desativado") + " com sucesso!"
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Erro: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "sucesso", false,
                "erro", e.getMessage()
            ));
        }
    }
}
