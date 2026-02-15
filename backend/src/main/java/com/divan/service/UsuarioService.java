package com.divan.service;

import com.divan.dto.*;
import com.divan.entity.Perfil;
import com.divan.entity.Permissao;
import com.divan.entity.Usuario;
import com.divan.repository.PerfilRepository;
import com.divan.repository.PermissaoRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PerfilRepository perfilRepository;
    
    @Autowired
    private PermissaoRepository permissaoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Usuario criar(UsuarioRequestDTO dto) {
        // Verificar se username já existe
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username já cadastrado");
        }
        
        // Verificar se email já existe
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setAtivo(dto.getAtivo());
        
        // ✅ LINHA 57 CORRIGIDA: Adicionar perfis
        if (dto.getPerfisIds() != null && !dto.getPerfisIds().isEmpty()) {
            List<Perfil> perfis = perfilRepository.findAllById(dto.getPerfisIds());
            usuario.setPerfis(new HashSet<>(perfis)); // ✅ Converter List para HashSet
        }
        
        // ✅ LINHA 63 CORRIGIDA: Adicionar permissões individuais
        if (dto.getPermissoesIds() != null && !dto.getPermissoesIds().isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findAllById(dto.getPermissoesIds());
            usuario.setPermissoes(new HashSet<>(permissoes)); // ✅ Converter List para HashSet
        }
        
        return usuarioRepository.save(usuario);
    }
    
    public Usuario atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setAtivo(dto.getAtivo());
        
        // ✅ LINHAS 80-82 CORRIGIDAS: Atualizar perfis
        if (dto.getPerfisIds() != null) {
            List<Perfil> perfis = perfilRepository.findAllById(dto.getPerfisIds());
            usuario.setPerfis(new HashSet<>(perfis)); // ✅ Converter List para HashSet
        } else {
            usuario.setPerfis(new HashSet<>()); // ✅ Usar HashSet vazio em vez de ArrayList
        }
        
        // ✅ LINHAS 88-90 CORRIGIDAS: Atualizar permissões individuais
        if (dto.getPermissoesIds() != null) {
            List<Permissao> permissoes = permissaoRepository.findAllById(dto.getPermissoesIds());
            usuario.setPermissoes(new HashSet<>(permissoes)); // ✅ Converter List para HashSet
        } else {
            usuario.setPermissoes(new HashSet<>()); // ✅ Usar HashSet vazio em vez de ArrayList
        }
        
        return usuarioRepository.save(usuario);
    }
    
    public void alterarSenha(Long id, AlterarSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar senha atual
        if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        
        // Atualizar senha
        usuario.setPassword(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
    }
    
    public void ativarDesativar(Long id, Boolean ativo) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorIdDTO(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return converterParaDTO(usuario);
    }
    
    private UsuarioResponseDTO converterParaDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setAtivo(usuario.getAtivo());
        dto.setDataCriacao(usuario.getDataCriacao());
        dto.setUltimoAcesso(usuario.getUltimoAcesso());
        
        // Converter perfis
        List<PerfilResponseDTO> perfisDTO = usuario.getPerfis().stream()
            .map(perfil -> {
                PerfilResponseDTO p = new PerfilResponseDTO();
                p.setId(perfil.getId());
                p.setNome(perfil.getNome());
                p.setDescricao(perfil.getDescricao());
                return p;
            })
            .collect(Collectors.toList());
        dto.setPerfis(perfisDTO);
        
        // Converter permissões
        List<PermissaoResponseDTO> permissoesDTO = usuario.getPermissoes().stream()
            .map(permissao -> {
                PermissaoResponseDTO p = new PermissaoResponseDTO();
                p.setId(permissao.getId());
                p.setNome(permissao.getNome());
                p.setDescricao(permissao.getDescricao());
                p.setCategoria(permissao.getCategoria());
                return p;
            })
            .collect(Collectors.toList());
        dto.setPermissoes(permissoesDTO);
        
        return dto;
    }
}
