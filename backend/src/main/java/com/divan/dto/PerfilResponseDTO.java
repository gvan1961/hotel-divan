package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


public class PerfilResponseDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    private List<PermissaoResponseDTO> permissoes;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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
	public List<PermissaoResponseDTO> getPermissoes() {
		return permissoes;
	}
	public void setPermissoes(List<PermissaoResponseDTO> permissoes) {
		this.permissoes = permissoes;
	}
    
    
}
