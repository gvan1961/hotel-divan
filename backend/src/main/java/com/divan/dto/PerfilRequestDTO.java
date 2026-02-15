package com.divan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public class PerfilRequestDTO {
    
    @NotBlank(message = "Nome do perfil é obrigatório")
    private String nome;
    
    private String descricao;
    
    private List<Long> permissoesIds;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public List<Long> getPermissoesIds() {
		return permissoesIds;
	}

	public void setPermissoesIds(List<Long> permissoesIds) {
		this.permissoesIds = permissoesIds;
	}
    
    
    
}
