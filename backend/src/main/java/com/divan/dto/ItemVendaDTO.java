package com.divan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class ItemVendaDTO {
    
    private Long id;
    
    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;
    
    private String produtoNome;
    
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    private Integer quantidade;
    
    @DecimalMin(value = "0.01", message = "Valor unitário deve ser maior que zero")
    private BigDecimal valorUnitario;
    
    @DecimalMin(value = "0.01", message = "Total do item deve ser maior que zero")
    private BigDecimal totalItem;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(Long produtoId) {
		this.produtoId = produtoId;
	}

	public String getProdutoNome() {
		return produtoNome;
	}

	public void setProdutoNome(String produtoNome) {
		this.produtoNome = produtoNome;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public BigDecimal getTotalItem() {
		return totalItem;
	}

	public void setTotalItem(BigDecimal totalItem) {
		this.totalItem = totalItem;
	}
    
    
}
