package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "controle_diarias")
public class ControleDiaria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnore
    private Reserva reserva;
    
    @Column(name = "data_lancamento", nullable = false)
    private LocalDateTime dataLancamento;
    
    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDiariaEnum status = StatusDiariaEnum.LANCADA;
    
    @Column(name = "extrato_id")
    private Long extratoId;
    
    @Column(name = "quantidade_hospedes")
    private Integer quantidadeHospedes;
    
    public enum StatusDiariaEnum {
        LANCADA,    // Diária lançada, aguardando fechamento às 12h01
        FECHADA,    // Diária fechada e cobrada
        CANCELADA   // Diária cancelada (ex: checkout antes de fechar)
    }

    // ========================================
    // GETTERS E SETTERS
    // ========================================
    
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

    public LocalDateTime getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDateTime dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusDiariaEnum getStatus() {
        return status;
    }

    public void setStatus(StatusDiariaEnum status) {
        this.status = status;
    }

    public Long getExtratoId() {
        return extratoId;
    }

    public void setExtratoId(Long extratoId) {
        this.extratoId = extratoId;
    }

    public Integer getQuantidadeHospedes() {
        return quantidadeHospedes;
    }

    public void setQuantidadeHospedes(Integer quantidadeHospedes) {
        this.quantidadeHospedes = quantidadeHospedes;
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
		ControleDiaria other = (ControleDiaria) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
