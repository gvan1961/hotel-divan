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
        // ✅ VALIDAR CNPJ
        if (empresa.getCnpj() != null && !empresa.getCnpj().isEmpty()) {
            if (!validarCNPJ(empresa.getCnpj())) {
                throw new RuntimeException("CNPJ inválido: " + empresa.getCnpj());
            }
            if (empresaRepository.existsByCnpj(empresa.getCnpj()) && empresa.getId() == null) {
                throw new RuntimeException("CNPJ já cadastrado");
            }
        }
        return empresaRepository.save(empresa);
    }

    private boolean validarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.isEmpty()) return true;

        String c = cnpj.toUpperCase().replaceAll("[^A-Z0-9]", "");

        if (c.length() != 14) return false;
        if (c.matches("(.)\\1{13}")) return false;

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < 12; i++)
            soma += charParaValor(c.charAt(i)) * pesos1[i];
        int dig1 = soma % 11;
        dig1 = dig1 < 2 ? 0 : 11 - dig1;

        soma = 0;
        for (int i = 0; i < 13; i++)
            soma += charParaValor(c.charAt(i)) * pesos2[i];
        int dig2 = soma % 11;
        dig2 = dig2 < 2 ? 0 : 11 - dig2;

        return charParaValor(c.charAt(12)) == dig1 &&
               charParaValor(c.charAt(13)) == dig2;
    }

    private int charParaValor(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        return c - 'A' + 10;
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
