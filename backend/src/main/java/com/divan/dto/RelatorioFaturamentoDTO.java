package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


public class RelatorioFaturamentoDTO {
    
    private LocalDate dataInicio;
    private LocalDate dataFim;
    
    // Receitas
    private BigDecimal receitaDiarias;
    private BigDecimal receitaProdutos;
    private BigDecimal receitaTotal;
    
    // Pagamentos por forma
    private BigDecimal pagamentoDinheiro;
    private BigDecimal pagamentoPix;
    private BigDecimal pagamentoCartaoDebito;
    private BigDecimal pagamentoCartaoCredito;
    private BigDecimal pagamentoTransferencia;
    private BigDecimal pagamentoFaturado;
    
    // Estatísticas
    private Integer quantidadeReservas;
    private Integer quantidadeCheckIns;
    private Integer quantidadeCheckOuts;
    private Integer quantidadeCancelamentos;
    
    // Ticket Médio
    private BigDecimal ticketMedio;
    private BigDecimal mediaHospedes;
    private BigDecimal mediaDiarias;
	public LocalDate getDataInicio() {
		return dataInicio;
	}
	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}
	public LocalDate getDataFim() {
		return dataFim;
	}
	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}
	public BigDecimal getReceitaDiarias() {
		return receitaDiarias;
	}
	public void setReceitaDiarias(BigDecimal receitaDiarias) {
		this.receitaDiarias = receitaDiarias;
	}
	public BigDecimal getReceitaProdutos() {
		return receitaProdutos;
	}
	public void setReceitaProdutos(BigDecimal receitaProdutos) {
		this.receitaProdutos = receitaProdutos;
	}
	public BigDecimal getReceitaTotal() {
		return receitaTotal;
	}
	public void setReceitaTotal(BigDecimal receitaTotal) {
		this.receitaTotal = receitaTotal;
	}
	public BigDecimal getPagamentoDinheiro() {
		return pagamentoDinheiro;
	}
	public void setPagamentoDinheiro(BigDecimal pagamentoDinheiro) {
		this.pagamentoDinheiro = pagamentoDinheiro;
	}
	public BigDecimal getPagamentoPix() {
		return pagamentoPix;
	}
	public void setPagamentoPix(BigDecimal pagamentoPix) {
		this.pagamentoPix = pagamentoPix;
	}
	public BigDecimal getPagamentoCartaoDebito() {
		return pagamentoCartaoDebito;
	}
	public void setPagamentoCartaoDebito(BigDecimal pagamentoCartaoDebito) {
		this.pagamentoCartaoDebito = pagamentoCartaoDebito;
	}
	public BigDecimal getPagamentoCartaoCredito() {
		return pagamentoCartaoCredito;
	}
	public void setPagamentoCartaoCredito(BigDecimal pagamentoCartaoCredito) {
		this.pagamentoCartaoCredito = pagamentoCartaoCredito;
	}
	public BigDecimal getPagamentoTransferencia() {
		return pagamentoTransferencia;
	}
	public void setPagamentoTransferencia(BigDecimal pagamentoTransferencia) {
		this.pagamentoTransferencia = pagamentoTransferencia;
	}
	public BigDecimal getPagamentoFaturado() {
		return pagamentoFaturado;
	}
	public void setPagamentoFaturado(BigDecimal pagamentoFaturado) {
		this.pagamentoFaturado = pagamentoFaturado;
	}
	public Integer getQuantidadeReservas() {
		return quantidadeReservas;
	}
	public void setQuantidadeReservas(Integer quantidadeReservas) {
		this.quantidadeReservas = quantidadeReservas;
	}
	public Integer getQuantidadeCheckIns() {
		return quantidadeCheckIns;
	}
	public void setQuantidadeCheckIns(Integer quantidadeCheckIns) {
		this.quantidadeCheckIns = quantidadeCheckIns;
	}
	public Integer getQuantidadeCheckOuts() {
		return quantidadeCheckOuts;
	}
	public void setQuantidadeCheckOuts(Integer quantidadeCheckOuts) {
		this.quantidadeCheckOuts = quantidadeCheckOuts;
	}
	public Integer getQuantidadeCancelamentos() {
		return quantidadeCancelamentos;
	}
	public void setQuantidadeCancelamentos(Integer quantidadeCancelamentos) {
		this.quantidadeCancelamentos = quantidadeCancelamentos;
	}
	public BigDecimal getTicketMedio() {
		return ticketMedio;
	}
	public void setTicketMedio(BigDecimal ticketMedio) {
		this.ticketMedio = ticketMedio;
	}
	public BigDecimal getMediaHospedes() {
		return mediaHospedes;
	}
	public void setMediaHospedes(BigDecimal mediaHospedes) {
		this.mediaHospedes = mediaHospedes;
	}
	public BigDecimal getMediaDiarias() {
		return mediaDiarias;
	}
	public void setMediaDiarias(BigDecimal mediaDiarias) {
		this.mediaDiarias = mediaDiarias;
	}
    
    
}
