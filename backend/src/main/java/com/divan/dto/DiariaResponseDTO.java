package com.divan.dto;

import com.divan.entity.TipoApartamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class DiariaResponseDTO {
    
    private Long id;
    private Long tipoApartamentoId;
    private TipoApartamento.TipoEnum tipoApartamento;
    private String descricaoTipoApartamento;
    private Integer quantidade;
    private BigDecimal valor;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTipoApartamentoId() {
		return tipoApartamentoId;
	}
	public void setTipoApartamentoId(Long tipoApartamentoId) {
		this.tipoApartamentoId = tipoApartamentoId;
	}
	public TipoApartamento.TipoEnum getTipoApartamento() {
		return tipoApartamento;
	}
	public void setTipoApartamento(TipoApartamento.TipoEnum tipoApartamento) {
		this.tipoApartamento = tipoApartamento;
	}
	public String getDescricaoTipoApartamento() {
		return descricaoTipoApartamento;
	}
	public void setDescricaoTipoApartamento(String descricaoTipoApartamento) {
		this.descricaoTipoApartamento = descricaoTipoApartamento;
	}
	public Integer getQuantidade() {
		return quantidade;
	}
	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
    
    
}
