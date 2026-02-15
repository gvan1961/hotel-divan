package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tipos_apartamento")

public class TipoApartamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEnum tipo;
    
    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false, length = 200)
    private String descricao;
    
    @OneToMany(mappedBy = "tipoApartamento", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("tipoApartamento")
    private List<Apartamento> apartamentos;
    
    @OneToMany(mappedBy = "tipoApartamento", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("tipoApartamento")
    private List<Diaria> diarias;
    
    public enum TipoEnum {
        A, B, C, D
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoEnum getTipo() {
		return tipo;
	}

	public void setTipo(TipoEnum tipo) {
		this.tipo = tipo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public List<Apartamento> getApartamentos() {
		return apartamentos;
	}

	public void setApartamentos(List<Apartamento> apartamentos) {
		this.apartamentos = apartamentos;
	}

	public List<Diaria> getDiarias() {
		return diarias;
	}

	public void setDiarias(List<Diaria> diarias) {
		this.diarias = diarias;
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
		TipoApartamento other = (TipoApartamento) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
