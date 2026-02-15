package com.divan.service;

import com.divan.dto.TipoApartamentoResponseDTO;
import com.divan.entity.TipoApartamento;
import com.divan.repository.TipoApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TipoApartamentoService {
    
    @Autowired
    private TipoApartamentoRepository tipoApartamentoRepository;
    
    public TipoApartamento salvar(TipoApartamento tipoApartamento) {
        if (tipoApartamentoRepository.existsByTipo(tipoApartamento.getTipo()) 
            && tipoApartamento.getId() == null) {
            throw new RuntimeException("Tipo de apartamento já cadastrado");
        }
        return tipoApartamentoRepository.save(tipoApartamento);
    }
    
    public TipoApartamento atualizar(Long id, TipoApartamento tipoApartamento) {
        Optional<TipoApartamento> tipoExistente = tipoApartamentoRepository.findById(id);
        if (tipoExistente.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        
        // Verificar se está tentando mudar para um tipo que já existe
        TipoApartamento tipoAtual = tipoExistente.get();
        if (!tipoAtual.getTipo().equals(tipoApartamento.getTipo()) 
            && tipoApartamentoRepository.existsByTipo(tipoApartamento.getTipo())) {
            throw new RuntimeException("Já existe um tipo de apartamento com este tipo");
        }
        
        tipoApartamento.setId(id);
        return tipoApartamentoRepository.save(tipoApartamento);
    }
    
    public void deletar(Long id) {
        TipoApartamento tipoApartamento = tipoApartamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tipo de apartamento não encontrado"));
        
        // Verificar se existem apartamentos usando este tipo
        if (tipoApartamento.getApartamentos() != null && !tipoApartamento.getApartamentos().isEmpty()) {
            throw new RuntimeException("Não é possível excluir. Existem apartamentos vinculados a este tipo");
        }
        
        // Verificar se existem diárias usando este tipo
        if (tipoApartamento.getDiarias() != null && !tipoApartamento.getDiarias().isEmpty()) {
            throw new RuntimeException("Não é possível excluir. Existem diárias vinculadas a este tipo");
        }
        
        tipoApartamentoRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<TipoApartamento> buscarPorId(Long id) {
        return tipoApartamentoRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<TipoApartamento> buscarPorTipo(TipoApartamento.TipoEnum tipo) {
        return tipoApartamentoRepository.findByTipo(tipo);
    }
    
    @Transactional(readOnly = true)
    public List<TipoApartamento> listarTodos() {
        return tipoApartamentoRepository.findAll();
    }
    
    private TipoApartamentoResponseDTO converterParaDTO(TipoApartamento tipo) {
        TipoApartamentoResponseDTO dto = new TipoApartamentoResponseDTO();
        dto.setId(tipo.getId());
        dto.setTipo(tipo.getTipo());
        dto.setDescricao(tipo.getDescricao());
        
        // Opcional: contar apartamentos deste tipo
        if (tipo.getApartamentos() != null) {
            dto.setTotalApartamentos(tipo.getApartamentos().size());
        } else {
            dto.setTotalApartamentos(0);
        }
        
        return dto;
    }
}
