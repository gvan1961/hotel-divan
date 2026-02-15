package com.divan.dto;

import com.divan.entity.ExtratoReserva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class ExtratoReservaDTO {
    
    private Long id;
    private Long reservaId;
    private LocalDateTime dataHoraLancamento;
    private ExtratoReserva.StatusLancamentoEnum statusLancamento;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal totalLancamento;
    private String descricao;
    private Long notaVendaId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
	public LocalDateTime getDataHoraLancamento() {
		return dataHoraLancamento;
	}
	public void setDataHoraLancamento(LocalDateTime dataHoraLancamento) {
		this.dataHoraLancamento = dataHoraLancamento;
	}
	public ExtratoReserva.StatusLancamentoEnum getStatusLancamento() {
		return statusLancamento;
	}
	public void setStatusLancamento(ExtratoReserva.StatusLancamentoEnum statusLancamento) {
		this.statusLancamento = statusLancamento;
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
	public BigDecimal getTotalLancamento() {
		return totalLancamento;
	}
	public void setTotalLancamento(BigDecimal totalLancamento) {
		this.totalLancamento = totalLancamento;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public Long getNotaVendaId() {
		return notaVendaId;
	}
	public void setNotaVendaId(Long notaVendaId) {
		this.notaVendaId = notaVendaId;
	}
    
    }
