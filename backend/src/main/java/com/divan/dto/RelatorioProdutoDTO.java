package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


public class RelatorioProdutoDTO {
    
    private Long produtoId;
    private String nomeProduto;
    private String categoria;
    private Integer quantidadeVendida;
    private BigDecimal valorTotal;
    private BigDecimal valorMedio;
    private Integer quantidadeVendas;
	public Long getProdutoId() {
		return produtoId;
	}
	public void setProdutoId(Long produtoId) {
		this.produtoId = produtoId;
	}
	public String getNomeProduto() {
		return nomeProduto;
	}
	public void setNomeProduto(String nomeProduto) {
		this.nomeProduto = nomeProduto;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
	public Integer getQuantidadeVendida() {
		return quantidadeVendida;
	}
	public void setQuantidadeVendida(Integer quantidadeVendida) {
		this.quantidadeVendida = quantidadeVendida;
	}
	public BigDecimal getValorTotal() {
		return valorTotal;
	}
	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}
	public BigDecimal getValorMedio() {
		return valorMedio;
	}
	public void setValorMedio(BigDecimal valorMedio) {
		this.valorMedio = valorMedio;
	}
	public Integer getQuantidadeVendas() {
		return quantidadeVendas;
	}
	public void setQuantidadeVendas(Integer quantidadeVendas) {
		this.quantidadeVendas = quantidadeVendas;
	}
    
    
}
