package com.divan.controller;

import com.divan.entity.Usuario;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private com.divan.repository.PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            Usuario u = new Usuario();
            u.setNome(body.get("nome").toString());
            u.setUsername(body.get("username").toString());
            u.setEmail(body.get("email").toString());
            u.setPassword(passwordEncoder.encode(body.get("password").toString()));
            u.setAtivo(Boolean.parseBoolean(body.getOrDefault("ativo", true).toString()));
            
            // ✅ PROCESSAR PERFIS no criar
            if (body.get("perfisIds") != null) {
                @SuppressWarnings("unchecked")
                List<Integer> perfisIds = (List<Integer>) body.get("perfisIds");
                List<Long> ids = perfisIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
                List<com.divan.entity.Perfil> perfis = perfilRepository.findAllById(ids);
                u.setPerfis(new java.util.HashSet<>(perfis));
            }
            
            return ResponseEntity.ok(usuarioRepository.save(u));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return usuarioRepository.findById(id).map(u -> {
            u.setNome(body.get("nome").toString());
            u.setUsername(body.get("username").toString());
            u.setEmail(body.get("email").toString());
            if (body.get("password") != null && !body.get("password").toString().isBlank()) {
                u.setPassword(passwordEncoder.encode(body.get("password").toString()));
            }
            u.setAtivo(Boolean.valueOf(body.getOrDefault("ativo", "true").toString()));

            // ✅ SALVAR PERFIS
            if (body.get("perfisIds") != null) {
                @SuppressWarnings("unchecked")
                List<Integer> perfisIds = (List<Integer>) body.get("perfisIds");
                List<Long> ids = perfisIds.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
                List<com.divan.entity.Perfil> perfis = perfilRepository.findAllById(ids);
                u.setPerfis(new java.util.HashSet<>(perfis));
            }

            return ResponseEntity.ok(usuarioRepository.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) return ResponseEntity.notFound().build();
        try {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", 
                "Usuário possui registros vinculados (caixa, reservas, etc.) e não pode ser excluído. Use a opção Inativar."));
        }
    }
}
