package com.divan.service;

import com.divan.entity.MovimentacaoEstoque;
import com.divan.entity.Produto;
import com.divan.entity.Fornecedor;
import com.divan.entity.Usuario;
import com.divan.repository.MovimentacaoEstoqueRepository;
import com.divan.repository.ProdutoRepository;
import com.divan.repository.FornecedorRepository;
import com.divan.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimentacaoEstoqueService(
            MovimentacaoEstoqueRepository movimentacaoRepository,
            ProdutoRepository produtoRepository,
            FornecedorRepository fornecedorRepository,
            UsuarioRepository usuarioRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MovimentacaoEstoque> listarTodas() {
        return movimentacaoRepository.findAllByOrderByCriadoEmDesc();
    }

    public List<MovimentacaoEstoque> listarPorProduto(Long produtoId) {
        return movimentacaoRepository.findByProdutoIdOrderByCriadoEmDesc(produtoId);
    }

    public List<MovimentacaoEstoque> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return movimentacaoRepository.findByPeriodo(inicio, fim);
    }

    @Transactional
    public MovimentacaoEstoque registrarEntrada(Long produtoId, Integer quantidade,
            BigDecimal valorUnitario, Long fornecedorId, String motivo) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        int quantidadeAnterior = produto.getQuantidade() != null ? produto.getQuantidade() : 0;
        int quantidadeNova = quantidadeAnterior + quantidade;

        // Atualizar produto
        produto.setQuantidade(quantidadeNova);
        if (valorUnitario != null) {
            produto.setValorCompra(valorUnitario);
        }
        produto.setDataUltimaCompra(LocalDateTime.now().toLocalDate());
        produtoRepository.save(produto);

        // Registrar movimentação
        return salvarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.ENTRADA,
                quantidadeAnterior, quantidade, quantidadeNova,
                valorUnitario, motivo, fornecedorId);
    }

    @Transactional
    public MovimentacaoEstoque registrarAcerto(Long produtoId, Integer quantidadeReal, String motivo) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        int quantidadeAnterior = produto.getQuantidade() != null ? produto.getQuantidade() : 0;
        int diferenca = quantidadeReal - quantidadeAnterior;

        // Atualizar produto
        produto.setQuantidade(quantidadeReal);
        produtoRepository.save(produto);

        // Registrar movimentação
        MovimentacaoEstoque.TipoMovimentacao tipo = diferenca >= 0
                ? MovimentacaoEstoque.TipoMovimentacao.ENTRADA
                : MovimentacaoEstoque.TipoMovimentacao.SAIDA;

        return salvarMovimentacao(produto, MovimentacaoEstoque.TipoMovimentacao.ACERTO,
                quantidadeAnterior, Math.abs(diferenca), quantidadeReal,
                null, motivo, null);
    }

    private MovimentacaoEstoque salvarMovimentacao(Produto produto, MovimentacaoEstoque.TipoMovimentacao tipo,
            int quantidadeAnterior, int quantidadeMovimentada, int quantidadeNova,
            BigDecimal valorUnitario, String motivo, Long fornecedorId) {

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setTipo(tipo);
        mov.setQuantidadeAnterior(quantidadeAnterior);
        mov.setQuantidadeMovimentada(quantidadeMovimentada);
        mov.setQuantidadeNova(quantidadeNova);
        mov.setValorUnitario(valorUnitario);
        mov.setMotivo(motivo);
        mov.setCriadoEm(LocalDateTime.now());

        if (fornecedorId != null) {
            fornecedorRepository.findById(fornecedorId).ifPresent(mov::setFornecedor);
        }

        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioRepository.findByUsername(login).ifPresent(mov::setUsuario);

        return movimentacaoRepository.save(mov);
    }
}