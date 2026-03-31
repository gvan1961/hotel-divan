package com.divan.service;

import com.divan.entity.*;
import com.divan.entity.DepositoProvisorio.StatusDeposito;
import com.divan.entity.NotaVenda.TipoVendaEnum;
import com.divan.entity.NotaVenda.Status;
import com.divan.repository.*;
import com.divan.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepositoProvisorioService {

	private final DepositoProvisorioRepository depositoRepository;
    private final DepositoProvisorioItemRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final ReservaRepository reservaRepository;
    private final NotaVendaRepository notaVendaRepository;
    private final ItemVendaRepository itemVendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExtratoReservaRepository extratoReservaRepository;

    public DepositoProvisorioService(
            DepositoProvisorioRepository depositoRepository,
            DepositoProvisorioItemRepository itemRepository,
            ProdutoRepository produtoRepository,
            ReservaRepository reservaRepository,
            NotaVendaRepository notaVendaRepository,
            ItemVendaRepository itemVendaRepository,
            UsuarioRepository usuarioRepository,
            ExtratoReservaRepository extratoReservaRepository) {
        this.depositoRepository = depositoRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.reservaRepository = reservaRepository;
        this.notaVendaRepository = notaVendaRepository;
        this.itemVendaRepository = itemVendaRepository;
        this.usuarioRepository = usuarioRepository;
        this.extratoReservaRepository = extratoReservaRepository;
    }
    @Transactional
    public DepositoProvisorio abrirOuRetornarDeposito() {
        List<DepositoProvisorio> abertos = depositoRepository.findByStatus(StatusDeposito.ABERTO);
        if (!abertos.isEmpty()) {
            return abertos.get(0);
        }
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(login)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        DepositoProvisorio novo = new DepositoProvisorio();
        novo.setUsuario(usuario);
        novo.setCriadoEm(LocalDateTime.now());
        novo.setStatus(StatusDeposito.ABERTO);
        return depositoRepository.save(novo);
    }

    public DepositoProvisorio getDepositoAtual() {
        List<DepositoProvisorio> abertos = depositoRepository.findByStatus(StatusDeposito.ABERTO);
        return abertos.isEmpty() ? null : abertos.get(0);
    }

    @Transactional
    public DepositoProvisorioItem adicionarItem(DepositoItemRequest request) {
        DepositoProvisorio deposito = abrirOuRetornarDeposito();

        Produto produto = produtoRepository.findById(request.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        DepositoProvisorioItem item = new DepositoProvisorioItem();
        item.setDeposito(deposito);
        item.setProduto(produto);
        item.setQuantidade(request.getQuantidade());
        item.setQuantidadeDistribuida(0);
        return itemRepository.save(item);
    }

    @Transactional
    public void distribuirItem(DepositoDistribuirRequest request) {
        DepositoProvisorioItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        int pendente = item.getQuantidade() - item.getQuantidadeDistribuida();
        if (request.getQuantidade() > pendente) {
            throw new RuntimeException("Quantidade maior que o saldo pendente: " + pendente);
        }

        Reserva reserva = reservaRepository.findById(request.getReservaId())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Produto produto = item.getProduto();
        BigDecimal valorUnitario = produto.getValorVenda();
        BigDecimal total = valorUnitario.multiply(BigDecimal.valueOf(request.getQuantidade()));

        // Cria NotaVenda do tipo APARTAMENTO
        NotaVenda nota = new NotaVenda();
        nota.setDataHoraVenda(LocalDateTime.now());
        nota.setTipoVenda(TipoVendaEnum.APARTAMENTO);
        nota.setReserva(reserva);
        nota.setTotal(total);
        nota.setStatus(Status.FECHADA);
        nota.setObservacao("Depósito provisório");
        notaVendaRepository.save(nota);

        // Cria ItemVenda
        ItemVenda itemVenda = new ItemVenda();
        itemVenda.setNotaVenda(nota);
        itemVenda.setProduto(produto);
        itemVenda.setQuantidade(request.getQuantidade());
        itemVenda.setValorUnitario(valorUnitario);
        itemVenda.setTotalItem(total);
        itemVendaRepository.save(itemVenda);
        
     // Atualiza estoque do produto
        produto.setQuantidade(produto.getQuantidade() - request.getQuantidade());
        produtoRepository.save(produto);
        
     // Cria ExtratoReserva
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(reserva);
        extrato.setDescricao("Consumo: " + produto.getNomeProduto());
        extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
        extrato.setQuantidade(request.getQuantidade());
        extrato.setValorUnitario(valorUnitario);
        extrato.setTotalLancamento(total);
        extrato.setDataHoraLancamento(LocalDateTime.now());
        extratoReservaRepository.save(extrato);

        // Atualiza totais da reserva
        reserva.setTotalProduto(reserva.getTotalProduto().add(total));
        reserva.setTotalHospedagem(reserva.getTotalHospedagem().add(total));
        reserva.setTotalApagar(reserva.getTotalApagar().add(total));
        reservaRepository.save(reserva);

        // Atualiza quantidade distribuída
        item.setQuantidadeDistribuida(item.getQuantidadeDistribuida() + request.getQuantidade());
        itemRepository.save(item);

        // Fecha depósito se tudo distribuído
        verificarEFecharDeposito(item.getDeposito());
    }

    @Transactional
    public void removerItem(Long itemId) {
        DepositoProvisorioItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        if (item.getQuantidadeDistribuida() > 0) {
            throw new RuntimeException("Item já possui distribuição parcial, não pode ser removido");
        }
        itemRepository.delete(item);
    }

    private void verificarEFecharDeposito(DepositoProvisorio deposito) {
        List<DepositoProvisorioItem> itens = itemRepository.findByDepositoId(deposito.getId());
        boolean tudoDistribuido = itens.stream()
                .allMatch(i -> i.getQuantidade().equals(i.getQuantidadeDistribuida()));

        if (tudoDistribuido) {
            deposito.setStatus(StatusDeposito.FECHADO);
            depositoRepository.save(deposito);
        }
    }
}