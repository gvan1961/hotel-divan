package com.divan.service;

import com.divan.dto.ComandaConsumoRequest;
import com.divan.dto.ComandaConsumoResponse;
import com.divan.dto.VendaBalcaoRequest;
import com.divan.dto.VendaBalcaoResponse;
import com.divan.entity.*;
import com.divan.enums.FormaPagamento;  // ✅ IMPORT CORRETO
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.divan.entity.ExtratoReserva;
import com.divan.repository.ExtratoReservaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VendaService {

    @Autowired
    private NotaVendaRepository notaVendaRepository;

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ContaAReceberRepository contaAReceberRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private CaixaValidacaoService caixaValidacaoService;

    // ============================================
    // ✅ MÉTODOS EXISTENTES (mantidos)
    // ============================================

    public List<NotaVenda> listarNotasVendaPorReserva(Long reservaId) {
        Optional<Reserva> reserva = reservaRepository.findById(reservaId);

        if (reserva.isPresent()) {
            return notaVendaRepository.findByReservaId(reservaId);
        }
        return List.of();
    }

    public List<NotaVenda> listarVendasDoDia(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);

        return notaVendaRepository.findByDataHoraVendaBetween(inicioDia, fimDia);
    }

    public List<NotaVenda> listarVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return notaVendaRepository.findByDataHoraVendaBetween(inicio, fim);
    }

    public List<NotaVenda> listarVendasVistaDelDia(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);

        return notaVendaRepository.findByTipoVendaAndDataHoraVendaBetween(
            NotaVenda.TipoVendaEnum.VISTA,
            inicioDia,
            fimDia
        );
    }

    // ============================================
    // 💵 VENDA À VISTA (BALCÃO)
    // ============================================

    @Transactional
    public VendaBalcaoResponse realizarVendaAVista(VendaBalcaoRequest request, Long usuarioId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💵 PROCESSANDO VENDA À VISTA");
        System.out.println("═══════════════════════════════════════════");
        
        // ✅ VALIDAR E BUSCAR CAIXA ABERTO
        FechamentoCaixa caixa = caixaValidacaoService.validarCaixaAberto(usuarioId);
        
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new RuntimeException("A venda deve conter pelo menos um item");
        }
        
        if (request.getFormaPagamento() == null || request.getFormaPagamento().isEmpty()) {
            throw new RuntimeException("Forma de pagamento é obrigatória");
        }
        
        NotaVenda nota = new NotaVenda();
        nota.setDataHoraVenda(LocalDateTime.now());
        nota.setTipoVenda(NotaVenda.TipoVendaEnum.VISTA);
        nota.setStatus(NotaVenda.Status.FECHADA);
        nota.setObservacao(request.getObservacao());
        nota.setItens(new ArrayList<>());
        
        BigDecimal totalVenda = BigDecimal.ZERO;
        
        for (VendaBalcaoRequest.ItemVenda itemReq : request.getItens()) {
            Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto #" + itemReq.getProdutoId() + " não encontrado"));
            
            if (produto.getQuantidade() < itemReq.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNomeProduto() + 
                                         ". Disponível: " + produto.getQuantidade());
            }
            
            BigDecimal valorUnitario = itemReq.getValorUnitario() != null ? 
                                      itemReq.getValorUnitario() : 
                                      produto.getValorVenda();
            
            BigDecimal totalItem = valorUnitario.multiply(new BigDecimal(itemReq.getQuantidade()));
            
            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(itemReq.getQuantidade());
            item.setValorUnitario(valorUnitario);
            item.setTotalItem(totalItem);
            item.setNotaVenda(nota);
            
            nota.getItens().add(item);
            totalVenda = totalVenda.add(totalItem);
            
            produto.setQuantidade(produto.getQuantidade() - itemReq.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("✅ Item: " + produto.getNomeProduto() + " x" + itemReq.getQuantidade() + 
                             " = R$ " + totalItem);
        }
        
        nota.setTotal(totalVenda);
        NotaVenda notaSalva = notaVendaRepository.save(nota);
        
        // ✅ CRIAR PAGAMENTO ASSOCIADO AO CAIXA
        Pagamento pagamento = new Pagamento();
        pagamento.setCaixa(caixa);  // ✅ ASSOCIAR AO CAIXA
        pagamento.setDataHora(LocalDateTime.now());
        pagamento.setValor(totalVenda);
        pagamento.setFormaPagamento(FormaPagamento.valueOf(request.getFormaPagamento()));
        pagamento.setDescricao("Venda balcão #" + notaSalva.getId());
        pagamento.setTipo("VENDA");
        pagamentoRepository.save(pagamento);
        
        System.out.println("💰 Total da venda: R$ " + totalVenda);
        System.out.println("✅ Venda à vista concluída!");
        System.out.println("═══════════════════════════════════════════");
        
        VendaBalcaoResponse response = new VendaBalcaoResponse();
        response.setNotaVendaId(notaSalva.getId());
        response.setDataHora(notaSalva.getDataHoraVenda());
        response.setTotal(totalVenda);
        response.setValorPago(request.getValorPago());
        response.setTroco(request.getValorPago() != null ? 
                         request.getValorPago().subtract(totalVenda) : 
                         BigDecimal.ZERO);
        response.setTipoVenda("VISTA");
        response.setFormaPagamento(request.getFormaPagamento());
        
        return response;
    }

    // ============================================
    // 💳 VENDA FATURADA (A PRAZO)
    // ============================================

    @Transactional
    public VendaBalcaoResponse realizarVendaFaturada(VendaBalcaoRequest request, Long usuarioId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💳 PROCESSANDO VENDA FATURADA");
        System.out.println("═══════════════════════════════════════════");
        
        // ✅ VALIDAR E BUSCAR CAIXA ABERTO
        FechamentoCaixa caixa = caixaValidacaoService.validarCaixaAberto(usuarioId);
        
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new RuntimeException("A venda deve conter pelo menos um item");
        }
        
        if (request.getClienteId() == null) {
            throw new RuntimeException("Cliente é obrigatório para venda faturada");
        }
        
        Cliente cliente = clienteRepository.findById(request.getClienteId())
            .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        
        if (cliente.getCreditoAprovado() == null || !cliente.getCreditoAprovado()) {
            throw new RuntimeException(
                "Cliente '" + cliente.getNome() + "' não possui crédito aprovado.\n\n" +
                "Aprove o crédito do cliente antes de realizar venda faturada."
            );
        }
        
        System.out.println("👤 Cliente: " + cliente.getNome());
        System.out.println("✅ Crédito aprovado: SIM");
        
        // Criar nota de venda
        NotaVenda nota = new NotaVenda();
        nota.setDataHoraVenda(LocalDateTime.now());
        nota.setTipoVenda(NotaVenda.TipoVendaEnum.FATURADO);
        nota.setStatus(NotaVenda.Status.FECHADA);
        nota.setObservacao(request.getObservacao());
        nota.setItens(new ArrayList<>());
        
        BigDecimal totalVenda = BigDecimal.ZERO;
        
        for (VendaBalcaoRequest.ItemVenda itemReq : request.getItens()) {
            Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto #" + itemReq.getProdutoId() + " não encontrado"));
            
            if (produto.getQuantidade() < itemReq.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNomeProduto());
            }
            
            BigDecimal valorUnitario = itemReq.getValorUnitario() != null ? 
                                      itemReq.getValorUnitario() : 
                                      produto.getValorVenda();
            
            BigDecimal totalItem = valorUnitario.multiply(new BigDecimal(itemReq.getQuantidade()));
            
            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(itemReq.getQuantidade());
            item.setValorUnitario(valorUnitario);
            item.setTotalItem(totalItem);
            item.setNotaVenda(nota);
            
            nota.getItens().add(item);
            totalVenda = totalVenda.add(totalItem);
            
            produto.setQuantidade(produto.getQuantidade() - itemReq.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("✅ Item: " + produto.getNomeProduto() + " x" + itemReq.getQuantidade());
        }
        
        nota.setTotal(totalVenda);
        NotaVenda notaSalva = notaVendaRepository.save(nota);
        
        // ✅ REGISTRAR PAGAMENTO FATURADO NO CAIXA (NOVO!)
        Pagamento pagamento = new Pagamento();
        pagamento.setCaixa(caixa);
        pagamento.setReserva(null); // ✅ SEM RESERVA (venda avulsa)
        pagamento.setTipo("VENDA_AVULSA_FATURADA");
        pagamento.setFormaPagamento(FormaPagamento.FATURADO);
        pagamento.setValor(totalVenda);
        pagamento.setDataHora(LocalDateTime.now());
        pagamento.setDescricao("Venda balcão faturada #" + notaSalva.getId() + " - " + cliente.getNome());
        
        pagamentoRepository.save(pagamento);
        System.out.println("✅ Pagamento FATURADO registrado no caixa: R$ " + totalVenda);
        
        // Criar Conta a Receber
        ContaAReceber conta = new ContaAReceber();
        conta.setCliente(cliente);
        conta.setNotaVenda(notaSalva);
        conta.setValor(totalVenda);
        conta.setValorPago(BigDecimal.ZERO);
        conta.setSaldo(totalVenda);
        conta.setStatus(ContaAReceber.StatusContaEnum.PENDENTE);
        conta.setDataVencimento(LocalDate.now().plusDays(30));
        conta.setDataCriacao(LocalDateTime.now());
        conta.setDescricao("Venda balcão #" + notaSalva.getId() + " - " + cliente.getNome());
        conta.setObservacao(request.getObservacao());
        
        if (cliente.getEmpresa() != null) {
            conta.setEmpresa(cliente.getEmpresa());
        }
        
        contaAReceberRepository.save(conta);
        
        System.out.println("💰 Total da venda: R$ " + totalVenda);
        System.out.println("📋 Conta a Receber criada - Vencimento: " + conta.getDataVencimento());
        System.out.println("✅ Venda faturada concluída!");
        System.out.println("═══════════════════════════════════════════");
        
        VendaBalcaoResponse response = new VendaBalcaoResponse();
        response.setNotaVendaId(notaSalva.getId());
        response.setDataHora(notaSalva.getDataHoraVenda());
        response.setTotal(totalVenda);
        response.setValorPago(BigDecimal.ZERO);
        response.setTroco(BigDecimal.ZERO);
        response.setTipoVenda("FATURADO");
        response.setClienteNome(cliente.getNome());
        
        return response;
    }

    // ============================================
    // 🏨 VENDA PARA RESERVA (APARTAMENTO/CONSUMO) - ANTIGO
    // ============================================

    @Transactional
    public NotaVenda adicionarVendaParaReserva(Long reservaId, List<ItemVenda> itens) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🏨 PROCESSANDO VENDA PARA RESERVA");
        System.out.println("   Reserva ID: " + reservaId);
        System.out.println("═══════════════════════════════════════════");
        
        if (itens == null || itens.isEmpty()) {
            throw new RuntimeException("A venda deve conter pelo menos um item");
        }
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva #" + reservaId + " não encontrada"));
        
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Apenas reservas ATIVAS podem receber vendas. Status atual: " + reserva.getStatus());
        }
        
        System.out.println("✅ Reserva encontrada: #" + reserva.getId());
        System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
        System.out.println("   Cliente: " + reserva.getCliente().getNome());
        
        NotaVenda nota = new NotaVenda();
        nota.setDataHoraVenda(LocalDateTime.now());
        nota.setTipoVenda(NotaVenda.TipoVendaEnum.APARTAMENTO);
        nota.setStatus(NotaVenda.Status.FECHADA);
        nota.setReserva(reserva);
        nota.setItens(new ArrayList<>());
        
        BigDecimal totalVenda = BigDecimal.ZERO;
        
        for (ItemVenda itemReq : itens) {
            Produto produto = produtoRepository.findById(itemReq.getProduto().getId())
                .orElseThrow(() -> new RuntimeException("Produto #" + itemReq.getProduto().getId() + " não encontrado"));
            
            if (produto.getQuantidade() < itemReq.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNomeProduto() + 
                                         ". Disponível: " + produto.getQuantidade());
            }
            
            BigDecimal valorUnitario = produto.getValorVenda();
            BigDecimal totalItem = valorUnitario.multiply(new BigDecimal(itemReq.getQuantidade()));
            
            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(itemReq.getQuantidade());
            item.setValorUnitario(valorUnitario);
            item.setTotalItem(totalItem);
            item.setNotaVenda(nota);
            
            nota.getItens().add(item);
            totalVenda = totalVenda.add(totalItem);
            
            produto.setQuantidade(produto.getQuantidade() - itemReq.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("✅ Item: " + produto.getNomeProduto() + " x" + itemReq.getQuantidade() + 
                             " = R$ " + totalItem);
        }
        
        nota.setTotal(totalVenda);
        NotaVenda notaSalva = notaVendaRepository.save(nota);
        
        System.out.println("💰 Total da venda: R$ " + totalVenda);
        System.out.println("✅ Venda adicionada à reserva #" + reserva.getId());
        System.out.println("═══════════════════════════════════════════");
        
        return notaSalva;
    }

    // ============================================
    // 🏨 COMANDA DE CONSUMO (APARTAMENTO) - NOVO
    // ============================================

    @Transactional
    public ComandaConsumoResponse adicionarComandaConsumo(ComandaConsumoRequest request) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🏨 PROCESSANDO COMANDA DE CONSUMO");
        System.out.println("   Reserva ID: " + request.getReservaId());
        System.out.println("═══════════════════════════════════════════");
        
        if (request.getItens() == null || request.getItens().isEmpty()) {
            throw new RuntimeException("A comanda deve conter pelo menos um item");
        }
        
        if (request.getReservaId() == null) {
            throw new RuntimeException("Reserva é obrigatória para comanda de consumo");
        }
        
        Reserva reserva = reservaRepository.findById(request.getReservaId())
            .orElseThrow(() -> new RuntimeException("Reserva #" + request.getReservaId() + " não encontrada"));
        
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Apenas reservas ATIVAS podem receber consumos. Status atual: " + reserva.getStatus());
        }
        
        System.out.println("✅ Reserva encontrada: #" + reserva.getId());
        System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
        System.out.println("   Hóspede: " + reserva.getCliente().getNome());
        
        NotaVenda nota = new NotaVenda();
        nota.setDataHoraVenda(LocalDateTime.now());
        nota.setTipoVenda(NotaVenda.TipoVendaEnum.APARTAMENTO);
        nota.setStatus(NotaVenda.Status.FECHADA);
        nota.setReserva(reserva);
        nota.setObservacao(request.getObservacao());
        nota.setItens(new ArrayList<>());
        
        BigDecimal totalComanda = BigDecimal.ZERO;
        
        for (ComandaConsumoRequest.ItemConsumo itemReq : request.getItens()) {
            Produto produto = produtoRepository.findById(itemReq.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto #" + itemReq.getProdutoId() + " não encontrado"));
            
            if (produto.getQuantidade() < itemReq.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNomeProduto() + 
                                         ". Disponível: " + produto.getQuantidade());
            }
            
            BigDecimal valorUnitario = itemReq.getValorUnitario() != null ? 
                                      itemReq.getValorUnitario() : 
                                      produto.getValorVenda();
            
            BigDecimal totalItem = valorUnitario.multiply(new BigDecimal(itemReq.getQuantidade()));
            
            ItemVenda item = new ItemVenda();
            item.setProduto(produto);
            item.setQuantidade(itemReq.getQuantidade());
            item.setValorUnitario(valorUnitario);
            item.setTotalItem(totalItem);
            item.setNotaVenda(nota);
            
            nota.getItens().add(item);
            totalComanda = totalComanda.add(totalItem);
            
            produto.setQuantidade(produto.getQuantidade() - itemReq.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("✅ Item: " + produto.getNomeProduto() + " x" + itemReq.getQuantidade() + 
                             " = R$ " + totalItem);
        }
        
        nota.setTotal(totalComanda);
        NotaVenda notaSalva = notaVendaRepository.save(nota);
        
        System.out.println("📋 Criando lançamentos no extrato da reserva...");
        
        for (ItemVenda item : notaSalva.getItens()) {
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
            extrato.setDescricao("Comanda #" + notaSalva.getId() + " - " + item.getProduto().getNomeProduto());
            extrato.setQuantidade(item.getQuantidade());
            extrato.setValorUnitario(item.getValorUnitario());
            extrato.setTotalLancamento(item.getTotalItem());
            extrato.setDataHoraLancamento(LocalDateTime.now());
            
            extratoReservaRepository.save(extrato);
            
            System.out.println("   ✅ Extrato: " + extrato.getDescricao() + " = R$ " + extrato.getTotalLancamento());
        }
        
        BigDecimal totalProdutosAtual = reserva.getTotalProduto() != null ? reserva.getTotalProduto() : BigDecimal.ZERO;
        BigDecimal novoTotalProdutos = totalProdutosAtual.add(totalComanda);
        
        reserva.setTotalProduto(novoTotalProdutos);
        reserva.setTotalHospedagem(reserva.getTotalDiaria().add(novoTotalProdutos));
        reserva.setTotalApagar(reserva.getTotalHospedagem().subtract(reserva.getTotalRecebido()));
        
        reservaRepository.save(reserva);
        
        System.out.println("💰 Total da comanda: R$ " + totalComanda);
        System.out.println("📊 Novo total de produtos: R$ " + novoTotalProdutos);
        System.out.println("📊 Novo total hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("✅ Comanda adicionada à reserva #" + reserva.getId());
        System.out.println("═══════════════════════════════════════════");
        
        ComandaConsumoResponse response = new ComandaConsumoResponse();
        response.setNotaVendaId(notaSalva.getId());
        response.setReservaId(reserva.getId());
        response.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
        response.setNomeHospede(reserva.getCliente().getNome());
        response.setDataHora(notaSalva.getDataHoraVenda());
        response.setTotal(totalComanda);
        response.setObservacao(request.getObservacao());
        
        return response;
    }
}