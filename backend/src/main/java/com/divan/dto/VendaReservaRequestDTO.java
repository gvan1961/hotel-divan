package com.divan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class VendaReservaRequestDTO {
    
    @NotNull(message = "Reserva é obrigatória")
    private Long reservaId;
    
    @NotEmpty(message = "Deve ter pelo menos um item")
    private List<ItemVendaRequestDTO> itens;

	public Long getReservaId() {
		return reservaId;
	}

	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}

	public List<ItemVendaRequestDTO> getItens() {
		return itens;
	}

	public void setItens(List<ItemVendaRequestDTO> itens) {
		this.itens = itens;
	}
    
    
}