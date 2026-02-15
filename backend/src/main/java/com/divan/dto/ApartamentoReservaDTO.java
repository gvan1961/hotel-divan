package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public class ApartamentoReservaDTO {
    
    private String numeroApartamento;
    private String tipoApartamento;
    private String status;
    private List<ReservaMapaDTO> reservas;
	public String getNumeroApartamento() {
		return numeroApartamento;
	}
	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}
	public String getTipoApartamento() {
		return tipoApartamento;
	}
	public void setTipoApartamento(String tipoApartamento) {
		this.tipoApartamento = tipoApartamento;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ReservaMapaDTO> getReservas() {
		return reservas;
	}
	public void setReservas(List<ReservaMapaDTO> reservas) {
		this.reservas = reservas;
	}
            
    
}
