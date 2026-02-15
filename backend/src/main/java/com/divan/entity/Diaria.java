package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "diarias", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_apartamento_id", "quantidade"}))

public class Diaria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Tipo de apartamento é obrigatório")
    @ManyToOne
    @JoinColumn(name = "tipo_apartamento_id", nullable = false)
    @JsonIgnoreProperties({"diarias", "apartamentos"})
    private TipoApartamento tipoApartamento;
    
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidade;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @OneToMany(mappedBy = "diaria", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("diaria")
    private List<Reserva> reservas;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoApartamento getTipoApartamento() {
		return tipoApartamento;
	}

	public void setTipoApartamento(TipoApartamento tipoApartamento) {
		this.tipoApartamento = tipoApartamento;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
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
		Diaria other = (Diaria) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
