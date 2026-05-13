package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "permissoes")

public class Permissao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da permissão é obrigatório")
    @Column(unique = true, nullable = false, length = 100)
    private String nome; // RESERVA_CREATE, RESERVA_READ, RESERVA_UPDATE, etc.
    
    @Column(length = 200)
    private String descricao;
    
    @Column(length = 50)
    private String categoria; // RESERVAS, CLIENTES, PRODUTOS, FINANCEIRO, etc.
    
    @ManyToMany(mappedBy = "permissoes")
    @JsonIgnoreProperties("permissoes")
    private List<Perfil> perfis = new ArrayList<>();
    
    @ManyToMany(mappedBy = "permissoes")
    @JsonIgnoreProperties({"perfis", "permissoes"})
    private List<Usuario> usuarios = new ArrayList<>();

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

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public List<Perfil> getPerfis() {
		return perfis;
	}

	public void setPerfis(List<Perfil> perfis) {
		this.perfis = perfis;
	}

	public List<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Permissao other = (Permissao) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
