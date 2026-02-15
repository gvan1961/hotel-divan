package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public class HistoricoHospedeDTO {
    
    private Long id;
    private LocalDateTime dataHora;
    private Integer quantidadeAnterior;
    private Integer quantidadeNova;
    private String motivo;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public LocalDateTime getDataHora() {
		return dataHora;
	}
	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}
	public Integer getQuantidadeAnterior() {
		return quantidadeAnterior;
	}
	public void setQuantidadeAnterior(Integer quantidadeAnterior) {
		this.quantidadeAnterior = quantidadeAnterior;
	}
	public Integer getQuantidadeNova() {
		return quantidadeNova;
	}
	public void setQuantidadeNova(Integer quantidadeNova) {
		this.quantidadeNova = quantidadeNova;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
        
}
