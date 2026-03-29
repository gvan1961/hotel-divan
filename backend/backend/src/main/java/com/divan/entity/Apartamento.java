package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "apartamentos")

public class Apartamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Número do apartamento é obrigatório")
    @Column(unique = true, nullable = false, length = 10)
    private String numeroApartamento;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_apartamento_id", nullable = false)
    @NotNull(message = "Tipo de apartamento é obrigatório")
    @JsonIgnoreProperties("apartamentos")
    private TipoApartamento tipoApartamento;
    
    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser no mínimo 1 pessoa")
    @Column(nullable = false)
    private Integer capacidade;
    
    // ✅✅✅ CORRIGIR AQUI - camelCase ✅✅✅
    @NotBlank(message = "Descrição das camas é obrigatória")
    @Column(name = "camas_do_apartamento", nullable = false, length = 200)
    private String camasDoApartamento; // ✅ CORRETO - camelCase
    
   
    
    @Column(length =200)
    private String tv;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEnum status = StatusEnum.DISPONIVEL;
    
    @OneToMany(mappedBy = "apartamento")
    @JsonIgnoreProperties("apartamento")
    private List<Reserva> reservas;
    
    public enum StatusEnum {
        DISPONIVEL,
        OCUPADO,
        LIMPEZA,
        PRE_RESERVA,
        MANUTENCAO,
        INDISPONIVEL        
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumeroApartamento() {
		return numeroApartamento;
	}

	public void setNumeroApartamento(String numeroApartamento) {
		this.numeroApartamento = numeroApartamento;
	}

	public TipoApartamento getTipoApartamento() {
		return tipoApartamento;
	}

	public void setTipoApartamento(TipoApartamento tipoApartamento) {
		this.tipoApartamento = tipoApartamento;
	}

	public Integer getCapacidade() {
		return capacidade;
	}

	public void setCapacidade(Integer capacidade) {
		this.capacidade = capacidade;
	}

	public String getCamasDoApartamento() {
		return camasDoApartamento;
	}

	public void setCamasDoApartamento(String camasDoApartamento) {
		this.camasDoApartamento = camasDoApartamento;
	}

	public String getTv() {
		return tv;
	}

	public void setTv(String tv) {
		this.tv = tv;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public List<Reserva> getReservas() {
		return reservas;
	}

	public void setReservas(List<Reserva> reservas) {
		this.reservas = reservas;
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
		Apartamento other = (Apartamento) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
