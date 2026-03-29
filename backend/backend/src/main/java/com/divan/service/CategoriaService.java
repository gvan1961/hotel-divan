package com.divan.service;

import com.divan.dto.CategoriaRequestDTO;
import com.divan.dto.CategoriaResponseDTO;
import com.divan.entity.Categoria;
import com.divan.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaService {
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public Categoria criar(CategoriaRequestDTO dto) {
        // Verificar se categoria já existe
        if (categoriaRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Categoria já cadastrada com este nome");
        }
        
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        
        return categoriaRepository.save(categoria);
    }
    
    public Categoria atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        
        // Verificar se o novo nome já existe em outra categoria
        if (!categoria.getNome().equals(dto.getNome()) && 
            categoriaRepository.existsByNome(dto.getNome())) {
            throw new RuntimeException("Já existe outra categoria com este nome");
        }
        
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        
        return categoriaRepository.save(categoria);
    }
    
    public void deletar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        
        // Verificar se há produtos vinculados
        if (categoria.getProdutos() != null && !categoria.getProdutos().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com produtos vinculados");
        }
        
        categoriaRepository.delete(categoria);
    }
    
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        return converterParaDTO(categoria);
    }
    
    @Transactional(readOnly = true)
    public Categoria buscarEntidadePorId(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
    }
    
    private CategoriaResponseDTO converterParaDTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        dto.setDescricao(categoria.getDescricao());
        dto.setTotalProdutos(categoria.getProdutos() != null ? categoria.getProdutos().size() : 0);
        return dto;
    }
}
