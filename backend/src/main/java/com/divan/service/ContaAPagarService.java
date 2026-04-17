package com.divan.service;

import com.divan.entity.ContaAPagar;
import com.divan.entity.Fornecedor;
import com.divan.entity.Usuario;
import com.divan.repository.ContaAPagarRepository;
import com.divan.repository.FornecedorRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContaAPagarService {

    private final ContaAPagarRepository contaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;

    public ContaAPagarService(ContaAPagarRepository contaRepository,
                               FornecedorRepository fornecedorRepository,
                               UsuarioRepository usuarioRepository) {
        this.contaRepository = contaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ContaAPagar> listarTodas() {
        return contaRepository.findAll();
    }

    public List<ContaAPagar> listarEmAberto() {
        return contaRepository.findContasEmAberto();
    }

    public List<ContaAPagar> listarVencidas() {
        return contaRepository.findContasVencidas(LocalDate.now());
    }

    public Optional<ContaAPagar> buscarPorId(Long id) {
        return contaRepository.findById(id);
    }

    @Transactional
    public ContaAPagar salvar(ContaAPagar conta, Long fornecedorId) {
        // Usuário logado
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioRepository.findByUsername(login).ifPresent(conta::setUsuario);

        // Fornecedor
        if (fornecedorId != null) {
            fornecedorRepository.findById(fornecedorId).ifPresent(conta::setFornecedorObj);
        }

        conta.setCriadoEm(LocalDateTime.now());
        conta.setSaldo(conta.getValor().subtract(conta.getValorPago() != null ? conta.getValorPago() : BigDecimal.ZERO));

        return contaRepository.save(conta);
    }

    @Transactional
    public ContaAPagar registrarPagamento(Long id, BigDecimal valorPago, String formaPagamento) {
        ContaAPagar conta = contaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        BigDecimal totalPago = (conta.getValorPago() != null ? conta.getValorPago() : BigDecimal.ZERO).add(valorPago);
        conta.setValorPago(totalPago);
        conta.setSaldo(conta.getValor().subtract(totalPago));
        conta.setFormaPagamento(formaPagamento);
        conta.setDataPagamento(LocalDate.now());

        if (conta.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            conta.setStatus(ContaAPagar.StatusContaEnum.PAGA);
        }

        return contaRepository.save(conta);
    }

    @Transactional
    public void atualizarVencidas() {
        List<ContaAPagar> vencidas = contaRepository.findContasVencidas(LocalDate.now());
        for (ContaAPagar conta : vencidas) {
            conta.setStatus(ContaAPagar.StatusContaEnum.VENCIDA);
            contaRepository.save(conta);
        }
    }

    @Transactional
    public ContaAPagar atualizar(Long id, ContaAPagar contaAtualizada, Long fornecedorId) {
        ContaAPagar conta = contaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        conta.setDescricao(contaAtualizada.getDescricao());
        conta.setValor(contaAtualizada.getValor());
        conta.setDataVencimento(contaAtualizada.getDataVencimento());
        conta.setCategoria(contaAtualizada.getCategoria());
        conta.setCodigoBarras(contaAtualizada.getCodigoBarras());
        conta.setObservacao(contaAtualizada.getObservacao());
        conta.setFornecedor(contaAtualizada.getFornecedor());
        conta.setSaldo(contaAtualizada.getValor().subtract(conta.getValorPago() != null ? conta.getValorPago() : BigDecimal.ZERO));

        if (fornecedorId != null) {
            fornecedorRepository.findById(fornecedorId).ifPresent(conta::setFornecedorObj);
        } else {
            conta.setFornecedorObj(null);
        }

        return contaRepository.save(conta);
    }

    public void deletar(Long id) {
        contaRepository.deleteById(id);
    }
}
