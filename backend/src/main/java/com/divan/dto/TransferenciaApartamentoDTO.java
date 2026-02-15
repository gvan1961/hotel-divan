package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public class TransferenciaApartamentoDTO {
    
    private Long reservaId;
    private Long novoApartamentoId;
    private LocalDateTime dataTransferencia; // null = imediato
    private String motivo;
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
	public Long getNovoApartamentoId() {
		return novoApartamentoId;
	}
	public void setNovoApartamentoId(Long novoApartamentoId) {
		this.novoApartamentoId = novoApartamentoId;
	}
	public LocalDateTime getDataTransferencia() {
		return dataTransferencia;
	}
	public void setDataTransferencia(LocalDateTime dataTransferencia) {
		this.dataTransferencia = dataTransferencia;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}    
        
    
}
