package com.divan.service;

import com.divan.entity.Diaria;
import com.divan.entity.TipoApartamento;
import com.divan.repository.DiariaRepository;
import com.divan.repository.TipoApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DiariaService {
    
    @Autowired
    private DiariaRepository diariaRepository;
    
    @Autowired
    private TipoApartamentoRepository tipoApartamentoRepository;
    
    public Diaria salvar(Diaria diaria, Long tipoApartamentoId) {
        // Buscar tipo de apartamento
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        
        diaria.setTipoApartamento(tipoOpt.get());
        
        // Verificar se já existe
        if (diariaRepository.existsByTipoApartamentoAndQuantidade(
                diaria.getTipoApartamento(), diaria.getQuantidade()) 
            && diaria.getId() == null) {
            throw new RuntimeException("Já existe diária para este tipo e quantidade");
        }
        
        return diariaRepository.save(diaria);
    }
    
    public Diaria atualizar(Long id, Diaria diaria, Long tipoApartamentoId) {
        Optional<Diaria> diariaExistente = diariaRepository.findById(id);
        if (diariaExistente.isEmpty()) {
            throw new RuntimeException("Diária não encontrada");
        }
        
        // Buscar tipo de apartamento
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        
        diaria.setId(id);
        diaria.setTipoApartamento(tipoOpt.get());
        
        return diariaRepository.save(diaria);
    }
    
    public void deletar(Long id) {
        if (!diariaRepository.existsById(id)) {
            throw new RuntimeException("Diária não encontrada");
        }
        diariaRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Diaria> buscarPorId(Long id) {
        return diariaRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Diaria> buscarPorTipoEQuantidade(Long tipoApartamentoId, Integer quantidade) {
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        return diariaRepository.findByTipoApartamentoAndQuantidade(tipoOpt.get(), quantidade);
    }
    
    @Transactional(readOnly = true)
    public List<Diaria> listarTodas() {
        return diariaRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Diaria> buscarPorTipo(Long tipoApartamentoId) {
        Optional<TipoApartamento> tipoOpt = tipoApartamentoRepository.findById(tipoApartamentoId);
        if (tipoOpt.isEmpty()) {
            throw new RuntimeException("Tipo de apartamento não encontrado");
        }
        return diariaRepository.findByTipoApartamentoOrderByQuantidade(tipoOpt.get());
    }
}
