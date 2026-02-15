package com.divan.service;

import java.util.Optional;
import com.divan.dto.EstornoRequest;
import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class EstornoService {

    @Autowired
    private EstornoRepository estornoRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private NotaVendaRepository notaVendaRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ContaAReceberRepository contaAReceberRepository;

    /**
     * 🔄 ESTORNAR CONSUMO DE APARTAMENTO (RESERVA)
     */
    @Transactional
    public void estornarConsumoApartamento(EstornoRequest request) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 PROCESSANDO ESTORNO - CONSUMO APARTAMENTO");
        System.out.println("   Extrato ID: " + request.getExtratoId());
        System.out.println("═══════════════════════════════════════════");
        
        // Validações
        if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
            throw new RuntimeException("Motivo do estorno é obrigatório");
        }
        
        // Buscar lançamento original
        ExtratoReserva extratoOriginal = extratoReservaRepository.findById(request.getExtratoId())
            .orElseThrow(() -> new RuntimeException("Lançamento #" + request.getExtratoId() + " não encontrado"));
        
        // Validar se é lançamento de PRODUTO
        if (extratoOriginal.getStatusLancamento() != ExtratoReserva.StatusLancamentoEnum.PRODUTO) {
            throw new RuntimeException("Apenas lançamentos de PRODUTO podem ser estornados");
        }
        
        Reserva reserva = extratoOriginal.getReserva();
        
        System.out.println("✅ Lançamento original encontrado:");
        System.out.println("   Descrição: " + extratoOriginal.getDescricao());
        System.out.println("   Valor: R$ " + extratoOriginal.getTotalLancamento());
        System.out.println("   Nota Venda ID: " + extratoOriginal.getNotaVendaId());
        
        // ✅ TENTAR BUSCAR O PRODUTO (PODE SER NULL)
        Produto produto = extratoOriginal.getProduto();

        if (produto != null) {
            System.out.println("✅ Produto identificado: " + produto.getNomeProduto());
            System.out.println("📦 Estoque ANTES: " + produto.getQuantidade());
            
            produto.setQuantidade(produto.getQuantidade() + extratoOriginal.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("📦 Estoque DEPOIS: " + produto.getQuantidade());
            System.out.println("✅ ESTOQUE DEVOLVIDO COM SUCESSO!");
        } else {
            System.out.println("⚠️ Lançamento sem produto vinculado (pode ser diária ou outro tipo)");
        }
        
        // Criar lançamento de ESTORNO no extrato
        ExtratoReserva extratoEstorno = new ExtratoReserva();
        extratoEstorno.setReserva(reserva);
        extratoEstorno.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
        extratoEstorno.setDescricao("ESTORNO: " + extratoOriginal.getDescricao() + " - Motivo: " + request.getMotivo());
        extratoEstorno.setQuantidade(extratoOriginal.getQuantidade());
        extratoEstorno.setValorUnitario(extratoOriginal.getValorUnitario());
        extratoEstorno.setTotalLancamento(extratoOriginal.getTotalLancamento().negate());
        extratoEstorno.setDataHoraLancamento(LocalDateTime.now());
        extratoEstorno.setNotaVendaId(extratoOriginal.getNotaVendaId());
        
        extratoReservaRepository.save(extratoEstorno);
        
        System.out.println("✅ Lançamento de estorno criado: R$ " + extratoEstorno.getTotalLancamento());
        
        // ✅ Registrar na tabela de estornos (PRODUTO PODE SER NULL)
        Estorno estorno = new Estorno();
        estorno.setReserva(reserva);
        estorno.setExtratoOriginal(extratoOriginal);
        estorno.setProduto(produto); // ✅ PODE SER NULL
        estorno.setQuantidade(extratoOriginal.getQuantidade());
        estorno.setValorUnitario(extratoOriginal.getValorUnitario());
        estorno.setValorTotal(extratoOriginal.getTotalLancamento());
        estorno.setMotivo(request.getMotivo());
        estorno.setTipoEstorno(Estorno.TipoEstornoEnum.RESERVA_APARTAMENTO);
        estorno.setDataHoraEstorno(LocalDateTime.now());
        estorno.setUsuario("Sistema");
        
        estornoRepository.save(estorno);
        
        // Recalcular totais da reserva
        BigDecimal totalProdutos = reserva.getTotalProduto().subtract(extratoOriginal.getTotalLancamento());
        reserva.setTotalProduto(totalProdutos);
        reserva.setTotalHospedagem(reserva.getTotalDiaria().add(totalProdutos));
        reserva.setTotalApagar(reserva.getTotalHospedagem().subtract(reserva.getTotalRecebido()));
        
        reservaRepository.save(reserva);
        
        System.out.println("📊 Totais recalculados:");
        System.out.println("   Total Produtos: R$ " + reserva.getTotalProduto());
        System.out.println("   Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("   Total a Pagar: R$ " + reserva.getTotalApagar());
        
        // Se solicitou criar lançamento correto
        if (Boolean.TRUE.equals(request.getCriarLancamentoCorreto()) && request.getCorrecao() != null) {
            criarLancamentoCorreto(reserva, request.getCorrecao());
        }
        
        System.out.println("✅ Estorno concluído com sucesso!");
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 🔄 ESTORNAR VENDA À VISTA
     */
    @Transactional
    public void estornarVendaAVista(EstornoRequest request) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 PROCESSANDO ESTORNO - VENDA À VISTA");
        System.out.println("   Nota Venda ID: " + request.getNotaVendaId());
        System.out.println("═══════════════════════════════════════════");
        
        if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
            throw new RuntimeException("Motivo do estorno é obrigatório");
        }
        
        NotaVenda nota = notaVendaRepository.findById(request.getNotaVendaId())
            .orElseThrow(() -> new RuntimeException("Nota de venda #" + request.getNotaVendaId() + " não encontrada"));
        
        if (nota.getTipoVenda() != NotaVenda.TipoVendaEnum.VISTA) {
            throw new RuntimeException("Esta nota não é de venda à vista");
        }
        
        // Devolver produtos ao estoque
        for (ItemVenda item : nota.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("📦 Devolvido ao estoque: " + produto.getNomeProduto() + " x" + item.getQuantidade());
            
            // Registrar estorno
            Estorno estorno = new Estorno();
            estorno.setNotaVenda(nota);
            estorno.setProduto(produto);
            estorno.setQuantidade(item.getQuantidade());
            estorno.setValorUnitario(item.getValorUnitario());
            estorno.setValorTotal(item.getTotalItem());
            estorno.setMotivo(request.getMotivo());
            estorno.setTipoEstorno(Estorno.TipoEstornoEnum.VENDA_VISTA);
            estorno.setDataHoraEstorno(LocalDateTime.now());
            estorno.setUsuario("Sistema");
            
            estornoRepository.save(estorno);
        }
        
        // Marcar nota como estornada
        nota.setStatus(NotaVenda.Status.ESTORNADA);
        nota.setObservacao((nota.getObservacao() != null ? nota.getObservacao() + " | " : "") + 
                          "ESTORNADA: " + request.getMotivo());
        notaVendaRepository.save(nota);
        
        System.out.println("✅ Venda à vista estornada. Total: R$ " + nota.getTotal());
        System.out.println("⚠️ ATENÇÃO: Devolução de R$ " + nota.getTotal() + " deve ser feita ao cliente!");
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * 🔄 ESTORNAR VENDA FATURADA (A PRAZO)
     */
    @Transactional
    public void estornarVendaFaturada(EstornoRequest request) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔄 PROCESSANDO ESTORNO - VENDA FATURADA");
        System.out.println("   Nota Venda ID: " + request.getNotaVendaId());
        System.out.println("═══════════════════════════════════════════");
        
        if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
            throw new RuntimeException("Motivo do estorno é obrigatório");
        }
        
        NotaVenda nota = notaVendaRepository.findById(request.getNotaVendaId())
            .orElseThrow(() -> new RuntimeException("Nota de venda #" + request.getNotaVendaId() + " não encontrada"));
        
        if (nota.getTipoVenda() != NotaVenda.TipoVendaEnum.FATURADO) {
            throw new RuntimeException("Esta nota não é de venda faturada");
        }
        
        // Buscar conta a receber
        ContaAReceber conta = contaAReceberRepository.findByNotaVendaId(nota.getId())
            .orElseThrow(() -> new RuntimeException("Conta a receber não encontrada para esta nota"));
        
        if (conta.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Não é possível estornar venda faturada que já teve pagamentos. " +
                                     "Valor pago: R$ " + conta.getValorPago());
        }
        
        // Devolver produtos ao estoque
        for (ItemVenda item : nota.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("📦 Devolvido ao estoque: " + produto.getNomeProduto() + " x" + item.getQuantidade());
            
            // Registrar estorno
            Estorno estorno = new Estorno();
            estorno.setNotaVenda(nota);
            estorno.setProduto(produto);
            estorno.setQuantidade(item.getQuantidade());
            estorno.setValorUnitario(item.getValorUnitario());
            estorno.setValorTotal(item.getTotalItem());
            estorno.setMotivo(request.getMotivo());
            estorno.setTipoEstorno(Estorno.TipoEstornoEnum.VENDA_FATURADA);
            estorno.setDataHoraEstorno(LocalDateTime.now());
            estorno.setUsuario("Sistema");
            
            estornoRepository.save(estorno);
        }
        
        // Marcar nota como estornada
        nota.setStatus(NotaVenda.Status.ESTORNADA);
        nota.setObservacao((nota.getObservacao() != null ? nota.getObservacao() + " | " : "") + 
                          "ESTORNADA: " + request.getMotivo());
        notaVendaRepository.save(nota);
        
        // Cancelar conta a receber
        conta.setStatus(ContaAReceber.StatusContaEnum.CANCELADA);
        conta.setObservacao((conta.getObservacao() != null ? conta.getObservacao() + " | " : "") + 
                           "ESTORNADA: " + request.getMotivo());
        contaAReceberRepository.save(conta);
        
        System.out.println("✅ Venda faturada estornada. Total: R$ " + nota.getTotal());
        System.out.println("✅ Conta a receber cancelada");
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * Criar lançamento correto após estorno
     */
    private void criarLancamentoCorreto(Reserva reserva, EstornoRequest.DadosCorrecao correcao) {
        Produto produto = produtoRepository.findById(correcao.getProdutoId())
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        
        BigDecimal valorTotal = produto.getValorVenda().multiply(new BigDecimal(correcao.getQuantidade()));
        
        ExtratoReserva novoLancamento = new ExtratoReserva();
        novoLancamento.setReserva(reserva);
        novoLancamento.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
        novoLancamento.setDescricao("CORREÇÃO: " + produto.getNomeProduto());
        novoLancamento.setQuantidade(correcao.getQuantidade());
        novoLancamento.setValorUnitario(produto.getValorVenda());
        novoLancamento.setTotalLancamento(valorTotal);
        novoLancamento.setDataHoraLancamento(LocalDateTime.now());
        
        extratoReservaRepository.save(novoLancamento);
        
        // Baixar estoque
        produto.setQuantidade(produto.getQuantidade() - correcao.getQuantidade());
        produtoRepository.save(produto);
        
        // Atualizar totais
        BigDecimal novoTotalProdutos = reserva.getTotalProduto().add(valorTotal);
        reserva.setTotalProduto(novoTotalProdutos);
        reserva.setTotalHospedagem(reserva.getTotalDiaria().add(novoTotalProdutos));
        reserva.setTotalApagar(reserva.getTotalHospedagem().subtract(reserva.getTotalRecebido()));
        
        reservaRepository.save(reserva);
        
        System.out.println("✅ Lançamento correto criado: " + produto.getNomeProduto() + " x" + correcao.getQuantidade());
    }
}
