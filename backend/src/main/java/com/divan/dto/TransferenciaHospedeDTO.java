package com.divan.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;



public class TransferenciaHospedeDTO {
    
    @NotNull(message = "ID do hóspede é obrigatório")
    private Long hospedeId;
    
    @NotNull(message = "ID do apartamento destino é obrigatório")
    private Long apartamentoDestinoId;
    
    @NotNull(message = "Data de check-in é obrigatória")
    private LocalDate dataCheckinNovo;
    
    @NotNull(message = "Data de check-out é obrigatória")
    private LocalDate dataCheckoutNovo;
    
    private Boolean pagarDespesasAntes = false; // Default: não paga antes

	public Long getHospedeId() {
		return hospedeId;
	}

	public void setHospedeId(Long hospedeId) {
		this.hospedeId = hospedeId;
	}

	public Long getApartamentoDestinoId() {
		return apartamentoDestinoId;
	}

	public void setApartamentoDestinoId(Long apartamentoDestinoId) {
		this.apartamentoDestinoId = apartamentoDestinoId;
	}

	public LocalDate getDataCheckinNovo() {
		return dataCheckinNovo;
	}

	public void setDataCheckinNovo(LocalDate dataCheckinNovo) {
		this.dataCheckinNovo = dataCheckinNovo;
	}

	public LocalDate getDataCheckoutNovo() {
		return dataCheckoutNovo;
	}

	public void setDataCheckoutNovo(LocalDate dataCheckoutNovo) {
		this.dataCheckoutNovo = dataCheckoutNovo;
	}

	public Boolean getPagarDespesasAntes() {
		return pagarDespesasAntes;
	}

	public void setPagarDespesasAntes(Boolean pagarDespesasAntes) {
		this.pagarDespesasAntes = pagarDespesasAntes;
	}
    
    
}