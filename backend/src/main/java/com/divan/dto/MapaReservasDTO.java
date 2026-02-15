package com.divan.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MapaReservasDTO {
    
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private List<ApartamentoReservaDTO> apartamentos;
	public LocalDateTime getDataInicio() {
		return dataInicio;
	}
	public void setDataInicio(LocalDateTime dataInicio) {
		this.dataInicio = dataInicio;
	}
	public LocalDateTime getDataFim() {
		return dataFim;
	}
	public void setDataFim(LocalDateTime dataFim) {
		this.dataFim = dataFim;
	}
	public List<ApartamentoReservaDTO> getApartamentos() {
		return apartamentos;
	}
	public void setApartamentos(List<ApartamentoReservaDTO> apartamentos) {
		this.apartamentos = apartamentos;
	}
	
    
    }
