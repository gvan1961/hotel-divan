package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cobranca_cartao")
public class CobrancaCartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private String formaPagamento; // credit_card ou debit_card

    @Column(name = "ordem_mercado_pago")
    private String ordemMercadoPago;

    @Column(name = "reserva_id")
    private Long reservaId;

    // ✅ VARCHAR em vez de enum — evita erro de truncamento se o Make
    // escrever uma palavra com caixa diferente (ex: "pago" vs "PAGO")
    @Column(nullable = false, length = 20)
    private String status = "PENDENTE";

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "itens_json", columnDefinition = "TEXT")
    private String itensJson;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }
    public String getOrdemMercadoPago() { return ordemMercadoPago; }
    public void setOrdemMercadoPago(String ordemMercadoPago) { this.ordemMercadoPago = ordemMercadoPago; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getItensJson() { return itensJson; }
    public void setItensJson(String itensJson) { this.itensJson = itensJson; }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CobrancaCartao other = (CobrancaCartao) obj;
        return Objects.equals(id, other.id);
    }
}
