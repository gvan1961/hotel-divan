package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "historico_hospedes")

public class HistoricoHospede {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Reserva é obrigatória")
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnore
    private Reserva reserva;
    
    @NotNull(message = "Data/hora é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHora;
    
    // CORREÇÃO: Permitir 0 para check-in inicial
    @Min(value = 0, message = "Quantidade anterior não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidadeAnterior;
    
    @Min(value = 1, message = "Quantidade nova deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidadeNova;
    
    @Column(length = 200)
    private String motivo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Reserva getReserva() {
		return reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}

	public Integer getQuantidadeAnterior() {
		return quantidadeAnterior;
	}

	public void setQuantidadeAnterior(Integer quantidadeAnterior) {
		this.quantidadeAnterior = quantidadeAnterior;
	}

	public Integer getQuantidadeNova() {
		return quantidadeNova;
	}

	public void setQuantidadeNova(Integer quantidadeNova) {
		this.quantidadeNova = quantidadeNova;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
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
		HistoricoHospede other = (HistoricoHospede) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
