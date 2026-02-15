package com.divan.dto;

import com.divan.entity.TipoApartamento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class TipoApartamentoResponseDTO {
    
    private Long id;
    private TipoApartamento.TipoEnum tipo;
    private String descricao;
    private Integer totalApartamentos; // Opcional: quantos apartamentos deste tipo existem
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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
	public Integer getTotalApartamentos() {
		return totalApartamentos;
	}
	public void setTotalApartamentos(Integer totalApartamentos) {
		this.totalApartamentos = totalApartamentos;
	}
    
    
}
