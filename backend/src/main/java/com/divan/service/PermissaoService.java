package com.divan.service;

import com.divan.dto.PermissaoRequestDTO;
import com.divan.dto.PermissaoResponseDTO;
import com.divan.entity.Permissao;
import com.divan.repository.PermissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissaoService {
    
    @Autowired
    private PermissaoRepository permissaoRepository;
    
    public Permissao criar(PermissaoRequestDTO dto) {
        if (permissaoRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Permissão já cadastrada");
        }
        
        Permissao permissao = new Permissao();
        permissao.setNome(dto.getNome());
        permissao.setDescricao(dto.getDescricao());
        permissao.setCategoria(dto.getCategoria());
        
        return permissaoRepository.save(permissao);
    }
    
    public Permissao atualizar(Long id, PermissaoRequestDTO dto) {
        Permissao permissao = permissaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));
        
        permissao.setNome(dto.getNome());
        permissao.setDescricao(dto.getDescricao());
        permissao.setCategoria(dto.getCategoria());
        
        return permissaoRepository.save(permissao);
    }
    
    public void deletar(Long id) {
        if (!permissaoRepository.existsById(id)) {
            throw new RuntimeException("Permissão não encontrada");
        }
        permissaoRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<PermissaoResponseDTO> listarTodas() {
        return permissaoRepository.findAll().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PermissaoResponseDTO> listarPorCategoria(String categoria) {
        return permissaoRepository.findByCategoria(categoria).stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PermissaoResponseDTO buscarPorId(Long id) {
        Permissao permissao = permissaoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));
        return converterParaDTO(permissao);
    }
    
    private PermissaoResponseDTO converterParaDTO(Permissao permissao) {
        PermissaoResponseDTO dto = new PermissaoResponseDTO();
        dto.setId(permissao.getId());
        dto.setNome(permissao.getNome());
        dto.setDescricao(permissao.getDescricao());
        dto.setCategoria(permissao.getCategoria());
        return dto;
    }
}
