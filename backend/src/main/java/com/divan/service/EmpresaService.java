package com.divan.service;

import com.divan.entity.Empresa;
import com.divan.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmpresaService {
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    public Empresa salvar(Empresa empresa) {
        if (empresaRepository.existsByCnpj(empresa.getCnpj()) && empresa.getId() == null) {
            throw new RuntimeException("CNPJ já cadastrado");
        }
        return empresaRepository.save(empresa);
    }
    
    public Empresa atualizar(Long id, Empresa empresa) {
        Optional<Empresa> empresaExistente = empresaRepository.findById(id);
        if (empresaExistente.isEmpty()) {
            throw new RuntimeException("Empresa não encontrada");
        }
        
        empresa.setId(id);
        return empresaRepository.save(empresa);
    }
    
    public void deletar(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new RuntimeException("Empresa não encontrada");
        }
        empresaRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Empresa> buscarPorId(Long id) {
        return empresaRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Empresa> buscarPorCnpj(String cnpj) {
        return empresaRepository.findByCnpj(cnpj);
    }
    
    @Transactional(readOnly = true)
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Empresa> buscarPorNome(String nome) {
        return empresaRepository.findByNomeEmpresaContainingIgnoreCase(nome);
    }
}
