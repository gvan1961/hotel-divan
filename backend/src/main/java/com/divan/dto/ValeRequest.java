package com.divan.dto;

import com.divan.entity.Vale;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ValeRequest {
    private Long clienteId;
    private LocalDate dataConcessao;
    private LocalDate dataVencimento;
    private Vale.TipoVale tipoVale;
    private BigDecimal valor;
    private String observacao;
	public Long getClienteId() {
		return clienteId;
	}
	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}
	public LocalDate getDataConcessao() {
		return dataConcessao;
	}
	public void setDataConcessao(LocalDate dataConcessao) {
		this.dataConcessao = dataConcessao;
	}
	public LocalDate getDataVencimento() {
		return dataVencimento;
	}
	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}
	public Vale.TipoVale getTipoVale() {
		return tipoVale;
	}
	public void setTipoVale(Vale.TipoVale tipoVale) {
		this.tipoVale = tipoVale;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
    
    
}
