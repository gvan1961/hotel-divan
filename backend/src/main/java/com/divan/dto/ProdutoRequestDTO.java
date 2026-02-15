package com.divan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


public class ProdutoRequestDTO {
    
    @NotBlank(message = "Nome do produto é obrigatório")
    private String nomeProduto;
    
    @NotNull(message = "Quantidade é obrigatória")
    @PositiveOrZero(message = "Quantidade deve ser maior ou igual a zero")
    private Integer quantidade;
    
    @NotNull(message = "Valor de venda é obrigatório")
    @PositiveOrZero(message = "Valor de venda deve ser maior ou igual a zero")
    private BigDecimal valorVenda;
    
    @NotNull(message = "Valor de compra é obrigatório")
    @PositiveOrZero(message = "Valor de compra deve ser maior ou igual a zero")
    private BigDecimal valorCompra;
    
    private Long categoriaId;  // ✅ APENAS O ID DA CATEGORIA

	public String getNomeProduto() {
		return nomeProduto;
	}

	public void setNomeProduto(String nomeProduto) {
		this.nomeProduto = nomeProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValorVenda() {
		return valorVenda;
	}

	public void setValorVenda(BigDecimal valorVenda) {
		this.valorVenda = valorVenda;
	}

	public BigDecimal getValorCompra() {
		return valorCompra;
	}

	public void setValorCompra(BigDecimal valorCompra) {
		this.valorCompra = valorCompra;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}
    
    
}
