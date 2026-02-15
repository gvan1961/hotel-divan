package com.divan.dto;

import com.divan.entity.TipoApartamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class TipoApartamentoRequestDTO {
    
    @NotNull(message = "Tipo é obrigatório")
    private TipoApartamento.TipoEnum tipo;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

	public TipoApartamento.TipoEnum getTipo() {
		return tipo;
	}

	public void setTipo(TipoApartamento.TipoEnum tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
    
    
}
