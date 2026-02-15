package com.divan.dto;

import com.divan.entity.Pagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoDTO {
    
    private Long id;
    
    @NotNull(message = "Reserva é obrigatória")
    private Long reservaId;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;
    
    private Pagamento.FormaPagamentoEnum formaPagamento;
    private LocalDateTime dataHoraPagamento;
    private String observacao;
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
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public Pagamento.FormaPagamentoEnum getFormaPagamento() {
		return formaPagamento;
	}
	public void setFormaPagamento(Pagamento.FormaPagamentoEnum formaPagamento) {
		this.formaPagamento = formaPagamento;
	}
	public LocalDateTime getDataHoraPagamento() {
		return dataHoraPagamento;
	}
	public void setDataHoraPagamento(LocalDateTime dataHoraPagamento) {
		this.dataHoraPagamento = dataHoraPagamento;
	}
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
    
    
}
