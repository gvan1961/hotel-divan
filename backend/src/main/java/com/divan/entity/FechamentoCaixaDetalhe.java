package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "fechamento_caixa_detalhes")
public class FechamentoCaixaDetalhe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fechamento_caixa_id", nullable = false)
    private FechamentoCaixa fechamentoCaixa;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(length = 255)
    private String descricao;

    @Column(name = "apartamento_numero", length = 20)  // ← ADICIONAR
    private String apartamentoNumero;

    @Column(name = "reserva_id")  // ← ADICIONAR
    private Long reservaId;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "forma_pagamento", length = 50)  // ← ADICIONAR
    private String formaPagamento;

    @Column(name = "data_hora")  // ← ADICIONAR
    private LocalDateTime dataHora;

    // GETTERS E SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FechamentoCaixa getFechamentoCaixa() {
        return fechamentoCaixa;
    }

    public void setFechamentoCaixa(FechamentoCaixa fechamentoCaixa) {
        this.fechamentoCaixa = fechamentoCaixa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getApartamentoNumero() {
        return apartamentoNumero;
    }

    public void setApartamentoNumero(String apartamentoNumero) {
        this.apartamentoNumero = apartamentoNumero;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
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
		FechamentoCaixaDetalhe other = (FechamentoCaixaDetalhe) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
