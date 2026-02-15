package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class CategoriaResponseDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Integer totalProdutos;
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
	public Integer getTotalProdutos() {
		return totalProdutos;
	}
	public void setTotalProdutos(Integer totalProdutos) {
		this.totalProdutos = totalProdutos;
	}
    
    
}
