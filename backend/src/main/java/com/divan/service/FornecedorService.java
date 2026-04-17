package com.divan.service;

import com.divan.entity.Fornecedor;
import com.divan.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public List<Fornecedor> listarAtivos() {
        return fornecedorRepository.findByAtivoTrue();
    }

    public List<Fornecedor> buscarPorNome(String nome) {
        return fornecedorRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Optional<Fornecedor> buscarPorId(Long id) {
        return fornecedorRepository.findById(id);
    }

    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        fornecedor.setCriadoEm(LocalDateTime.now());
        fornecedor.setAtivo(true);
        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(Long id, Fornecedor fornecedorAtualizado) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        fornecedor.setNome(fornecedorAtualizado.getNome());
        fornecedor.setCnpj(fornecedorAtualizado.getCnpj());
        fornecedor.setTelefone(fornecedorAtualizado.getTelefone());
        fornecedor.setEmail(fornecedorAtualizado.getEmail());
        fornecedor.setEndereco(fornecedorAtualizado.getEndereco());
        fornecedor.setObservacao(fornecedorAtualizado.getObservacao());

        return fornecedorRepository.save(fornecedor);
    }

    @Transactional
    public void inativar(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    public void deletar(Long id) {
        fornecedorRepository.deleteById(id);
    }
}
