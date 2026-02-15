package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ComandaConsumoResponse {
    private Long notaVendaId;
    private Long reservaId;
    private String numeroApartamento;
    private String nomeHospede;
    private LocalDateTime dataHora;
    private BigDecimal total;
    private String observacao;
	public Long getNotaVendaId() {
		return notaVendaId;
	}
	public void setNotaVendaId(Long notaVendaId) {
		this.notaVendaId = notaVendaId;
	}
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
	public String getNumeroApartamento() {
		return numeroApartamento;
	}
	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}
	public String getNomeHospede() {
		return nomeHospede;
	}
	public void setNomeHospede(String nomeHospede) {
		this.nomeHospede = nomeHospede;
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
	public String getObservacao() {
		return observacao;
	}
	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
    
       
}
