package com.divan.service;

import com.divan.dto.ConsumoJantarDTO;
import com.divan.entity.ExtratoReserva;
import com.divan.entity.Produto;
import com.divan.entity.Reserva;
import com.divan.repository.ExtratoReservaRepository;
import com.divan.repository.ProdutoRepository;
import com.divan.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ConsumoJantarService {
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Transactional
    public void lancarConsumo(ConsumoJantarDTO dto) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🍽️ LANÇANDO CONSUMO DE JANTAR");
        System.out.println("═══════════════════════════════════════════");
        
        // 1️⃣ Buscar reserva
        Reserva reserva = reservaRepository.findById(dto.getReservaId())
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        System.out.println("📋 Reserva #" + reserva.getId());
        System.out.println("   Apartamento: " + reserva.getApartamento().getNumeroApartamento());
        
        // 2️⃣ Processar cada item
        for (ConsumoJantarDTO.ItemConsumo item : dto.getItens()) {
            // Buscar produto
            Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            
            // Verificar estoque
            if (produto.getQuantidade() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNomeProduto() + 
                    ". Disponível: " + produto.getQuantidade());
            }
            
            // Calcular total
            BigDecimal valorUnitario = produto.getValorVenda();
            BigDecimal totalItem = valorUnitario.multiply(BigDecimal.valueOf(item.getQuantidade()));
            
            // Criar lançamento no extrato
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setDataHoraLancamento(LocalDateTime.now());
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
            
            // ✅ Descrição com observação (se houver)
            String descricao = produto.getNomeProduto();
            if (dto.getObservacao() != null && !dto.getObservacao().trim().isEmpty()) {
                descricao += " - " + dto.getObservacao();
            }
            extrato.setDescricao(descricao);
            
            extrato.setQuantidade(item.getQuantidade());
            extrato.setValorUnitario(valorUnitario);
            extrato.setTotalLancamento(totalItem);
            extrato.setNotaVendaId(null);
            
            extratoReservaRepository.save(extrato);
            
            System.out.println("✅ Lançamento criado:");
            System.out.println("   Produto: " + produto.getNomeProduto());
            System.out.println("   Quantidade: " + item.getQuantidade());
            System.out.println("   Valor unitário: R$ " + valorUnitario);
            System.out.println("   Total: R$ " + totalItem);
            
            // Atualizar estoque
            produto.setQuantidade(produto.getQuantidade() - item.getQuantidade());
            produtoRepository.save(produto);
            
            System.out.println("📦 Estoque atualizado: " + produto.getQuantidade());
        }
        
        // ═══════════════════════════════════════════════════════════════
        // ✨✨✨ CORREÇÃO DO BUG: RECALCULAR TOTAIS ✨✨✨
        // ═══════════════════════════════════════════════════════════════
        
        System.out.println("\n💰 RECALCULANDO TOTAIS DA RESERVA");
        System.out.println("════════════════════════════════════════");
        
        // Buscar TODOS os extratos da reserva
        List<ExtratoReserva> todosExtratos = extratoReservaRepository
            .findByReservaOrderByDataHoraLancamento(reserva);
        
        // ✅ SOMAR TODAS AS DIÁRIAS + ESTORNOS
        BigDecimal totalDiarias = BigDecimal.ZERO;
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.DIARIA ||
                extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.ESTORNO) {
                totalDiarias = totalDiarias.add(extrato.getTotalLancamento());
            }
        }
        
        // ✅ SOMAR TODOS OS PRODUTOS (PDV + JANTAR)
        BigDecimal totalProdutos = BigDecimal.ZERO;
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.PRODUTO ||
                extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.PENDENTE) {
                if (extrato.getTotalLancamento().compareTo(BigDecimal.ZERO) > 0) {
                    totalProdutos = totalProdutos.add(extrato.getTotalLancamento());
                }
            }
        }
        
        // ✅ ATUALIZAR TOTAIS DA RESERVA
        reserva.setTotalDiaria(totalDiarias);
        reserva.setTotalProduto(totalProdutos);
        reserva.setTotalHospedagem(totalDiarias.add(totalProdutos));
        
        // Recalcular saldo
        BigDecimal totalRecebido = reserva.getTotalRecebido() != null ? 
            reserva.getTotalRecebido() : BigDecimal.ZERO;
        reserva.setTotalApagar(reserva.getTotalHospedagem().subtract(totalRecebido));
        
        // Salvar reserva
        reservaRepository.save(reserva);
        
        System.out.println("💰 Total de diárias: R$ " + totalDiarias);
        System.out.println("🛒 Total de produtos: R$ " + totalProdutos);
        System.out.println("💵 Total hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("💳 Total a pagar: R$ " + reserva.getTotalApagar());
        
        // ═══════════════════════════════════════════════════════════════
        
        System.out.println("\n✅ CONSUMO LANÇADO COM SUCESSO!");
        System.out.println("═══════════════════════════════════════════");
    }
}

