package com.divan.service;

import com.divan.dto.PerfilRequestDTO;
import com.divan.dto.PerfilResponseDTO;
import com.divan.dto.PermissaoResponseDTO;
import com.divan.entity.Perfil;
import com.divan.entity.Permissao;
import com.divan.repository.PerfilRepository;
import com.divan.repository.PermissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerfilService {
    
    @Autowired
    private PerfilRepository perfilRepository;
    
    @Autowired
    private PermissaoRepository permissaoRepository;
    
    public Perfil criar(PerfilRequestDTO dto) {
        if (perfilRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Perfil já cadastrado");
        }
        
        Perfil perfil = new Perfil();
        perfil.setNome(dto.getNome());
        perfil.setDescricao(dto.getDescricao());
        
        if (dto.getPermissoesIds() != null && !dto.getPermissoesIds().isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findAllById(dto.getPermissoesIds());
          //  perfil.setPermissoes(permissoes);
        }
        
        return perfilRepository.save(perfil);
    }
    
    public Perfil atualizar(Long id, PerfilRequestDTO dto) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        
        perfil.setNome(dto.getNome());
        perfil.setDescricao(dto.getDescricao());
        
        if (dto.getPermissoesIds() != null) {
            List<Permissao> permissoes = permissaoRepository.findAllById(dto.getPermissoesIds());
            perfil.setPermissoes(permissoes);
        } else {
            perfil.setPermissoes(new ArrayList<>());
        }
        
        return perfilRepository.save(perfil);
    }
    
    public void deletar(Long id) {
        if (!perfilRepository.existsById(id)) {
            throw new RuntimeException("Perfil não encontrado");
        }
        perfilRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<PerfilResponseDTO> listarTodos() {
        return perfilRepository.findAll().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PerfilResponseDTO buscarPorId(Long id) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        return converterParaDTO(perfil);
    }
    
    private PerfilResponseDTO converterParaDTO(Perfil perfil) {
        PerfilResponseDTO dto = new PerfilResponseDTO();
        dto.setId(perfil.getId());
        dto.setNome(perfil.getNome());
        dto.setDescricao(perfil.getDescricao());
        
        List<PermissaoResponseDTO> permissoesDTO = perfil.getPermissoes().stream()
            .map(p -> {
                PermissaoResponseDTO perm = new PermissaoResponseDTO();
                perm.setId(p.getId());
                perm.setNome(p.getNome());
                perm.setDescricao(p.getDescricao());
                perm.setCategoria(p.getCategoria());
                return perm;
            })
            .collect(Collectors.toList());
        
        dto.setPermissoes(permissoesDTO);
        return dto;
    }
}
