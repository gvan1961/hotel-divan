package com.divan.dto;

import java.math.BigDecimal;
import java.util.Map;


public class ResumoPagamentosDTO {
    
    private BigDecimal totalDinheiro;
    private BigDecimal totalPix;
    private BigDecimal totalCartaoDebito;
    private BigDecimal totalCartaoCredito;
    private BigDecimal totalTransferencia;
    private BigDecimal totalFaturado;
    private BigDecimal totalGeral;
    private Integer quantidadePagamentos;
    private BigDecimal totalLinkPix = BigDecimal.ZERO;
    private BigDecimal totalLinkCartao = BigDecimal.ZERO;
	public BigDecimal getTotalDinheiro() {
		return totalDinheiro;
	}
	public void setTotalDinheiro(BigDecimal totalDinheiro) {
		this.totalDinheiro = totalDinheiro;
	}
	public BigDecimal getTotalPix() {
		return totalPix;
	}
	public void setTotalPix(BigDecimal totalPix) {
		this.totalPix = totalPix;
	}
	public BigDecimal getTotalCartaoDebito() {
		return totalCartaoDebito;
	}
	public void setTotalCartaoDebito(BigDecimal totalCartaoDebito) {
		this.totalCartaoDebito = totalCartaoDebito;
	}
	public BigDecimal getTotalCartaoCredito() {
		return totalCartaoCredito;
	}
	public void setTotalCartaoCredito(BigDecimal totalCartaoCredito) {
		this.totalCartaoCredito = totalCartaoCredito;
	}
	public BigDecimal getTotalTransferencia() {
		return totalTransferencia;
	}
	public void setTotalTransferencia(BigDecimal totalTransferencia) {
		this.totalTransferencia = totalTransferencia;
	}
	public BigDecimal getTotalFaturado() {
		return totalFaturado;
	}
	public void setTotalFaturado(BigDecimal totalFaturado) {
		this.totalFaturado = totalFaturado;
	}
	public BigDecimal getTotalGeral() {
		return totalGeral;
	}
	public void setTotalGeral(BigDecimal totalGeral) {
		this.totalGeral = totalGeral;
	}
	public Integer getQuantidadePagamentos() {
		return quantidadePagamentos;
	}
	public void setQuantidadePagamentos(Integer quantidadePagamentos) {
		this.quantidadePagamentos = quantidadePagamentos;
	}
	public BigDecimal getTotalLinkPix() {
		return totalLinkPix;
	}
	public void setTotalLinkPix(BigDecimal totalLinkPix) {
		this.totalLinkPix = totalLinkPix;
	}
	public BigDecimal getTotalLinkCartao() {
		return totalLinkCartao;
	}
	public void setTotalLinkCartao(BigDecimal totalLinkCartao) {
		this.totalLinkCartao = totalLinkCartao;
	}    
    
}
