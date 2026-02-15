package com.divan.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


public class VendaBalcaoResponse {
    private Long notaVendaId;
    private LocalDateTime dataHora;
    private BigDecimal total;
    private BigDecimal valorPago;
    private BigDecimal troco;
    private String tipoVenda; // VISTA ou FATURADO
    private String clienteNome; // Se for faturado
    private String formaPagamento; // Se for à vista
	public Long getNotaVendaId() {
		return notaVendaId;
	}
	public void setNotaVendaId(Long notaVendaId) {
		this.notaVendaId = notaVendaId;
	}
	public LocalDateTime getDataHora() {
		return dataHora;
	}
	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public BigDecimal getValorPago() {
		return valorPago;
	}
	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}
	public BigDecimal getTroco() {
		return troco;
	}
	public void setTroco(BigDecimal troco) {
		this.troco = troco;
	}
	public String getTipoVenda() {
		return tipoVenda;
	}
	public void setTipoVenda(String tipoVenda) {
		this.tipoVenda = tipoVenda;
	}
	public String getClienteNome() {
		return clienteNome;
	}
	public void setClienteNome(String clienteNome) {
		this.clienteNome = clienteNome;
	}
	public String getFormaPagamento() {
		return formaPagamento;
	}
	public void setFormaPagamento(String formaPagamento) {
		this.formaPagamento = formaPagamento;
	}
    
    
}
