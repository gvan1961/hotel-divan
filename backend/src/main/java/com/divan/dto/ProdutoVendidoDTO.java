package com.divan.dto;

import java.math.BigDecimal;

public class ProdutoVendidoDTO {
    
    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal totalItem;
    
    public ProdutoVendidoDTO() {}
    
    public ProdutoVendidoDTO(Long produtoId, String nomeProduto, Integer quantidade, 
                            BigDecimal valorUnitario, BigDecimal totalItem) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.totalItem = totalItem;
    }
    
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    
    public BigDecimal getTotalItem() { return totalItem; }
    public void setTotalItem(BigDecimal totalItem) { this.totalItem = totalItem; }
}
