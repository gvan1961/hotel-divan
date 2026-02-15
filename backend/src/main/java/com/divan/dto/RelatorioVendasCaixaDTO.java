package com.divan.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelatorioVendasCaixaDTO {
    
    private Long caixaId;
    private Map<String, List<VendaDetalhadaDTO>> vendasPorFormaPagamento = new HashMap<>();
    private Map<String, BigDecimal> totaisPorFormaPagamento = new HashMap<>();
    private Map<String, Integer> quantidadeVendasPorFormaPagamento = new HashMap<>();
    private Map<String, Integer> quantidadeProdutosPorFormaPagamento = new HashMap<>();
    private BigDecimal totalGeral = BigDecimal.ZERO;
    private Integer totalVendas = 0;
    private Integer totalProdutos = 0;
    
    public RelatorioVendasCaixaDTO() {
        inicializarMapas();
    }
    
    public RelatorioVendasCaixaDTO(Long caixaId) {
        this.caixaId = caixaId;
        inicializarMapas();
    }
    
    private void inicializarMapas() {
        String[] formasPagamento = {"DINHEIRO", "PIX", "CARTAO_DEBITO", 
                                   "CARTAO_CREDITO", "TRANSFERENCIA", "FATURADO"};
        
        for (String forma : formasPagamento) {
            vendasPorFormaPagamento.put(forma, new ArrayList<>());
            totaisPorFormaPagamento.put(forma, BigDecimal.ZERO);
            quantidadeVendasPorFormaPagamento.put(forma, 0);
            quantidadeProdutosPorFormaPagamento.put(forma, 0);
        }
    }
    
    public Long getCaixaId() { return caixaId; }
    public void setCaixaId(Long caixaId) { this.caixaId = caixaId; }
    
    public Map<String, List<VendaDetalhadaDTO>> getVendasPorFormaPagamento() { return vendasPorFormaPagamento; }
    public void setVendasPorFormaPagamento(Map<String, List<VendaDetalhadaDTO>> vendasPorFormaPagamento) { 
        this.vendasPorFormaPagamento = vendasPorFormaPagamento; 
    }
    
    public Map<String, BigDecimal> getTotaisPorFormaPagamento() { return totaisPorFormaPagamento; }
    public void setTotaisPorFormaPagamento(Map<String, BigDecimal> totaisPorFormaPagamento) { 
        this.totaisPorFormaPagamento = totaisPorFormaPagamento; 
    }
    
    public Map<String, Integer> getQuantidadeVendasPorFormaPagamento() { return quantidadeVendasPorFormaPagamento; }
    public void setQuantidadeVendasPorFormaPagamento(Map<String, Integer> quantidadeVendasPorFormaPagamento) { 
        this.quantidadeVendasPorFormaPagamento = quantidadeVendasPorFormaPagamento; 
    }
    
    public Map<String, Integer> getQuantidadeProdutosPorFormaPagamento() { return quantidadeProdutosPorFormaPagamento; }
    public void setQuantidadeProdutosPorFormaPagamento(Map<String, Integer> quantidadeProdutosPorFormaPagamento) { 
        this.quantidadeProdutosPorFormaPagamento = quantidadeProdutosPorFormaPagamento; 
    }
    
    public BigDecimal getTotalGeral() { return totalGeral; }
    public void setTotalGeral(BigDecimal totalGeral) { this.totalGeral = totalGeral; }
    
    public Integer getTotalVendas() { return totalVendas; }
    public void setTotalVendas(Integer totalVendas) { this.totalVendas = totalVendas; }
    
    public Integer getTotalProdutos() { return totalProdutos; }
    public void setTotalProdutos(Integer totalProdutos) { this.totalProdutos = totalProdutos; }
}
