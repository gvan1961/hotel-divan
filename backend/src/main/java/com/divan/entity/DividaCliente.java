package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dívida pessoal de um hóspede junto ao hotel — usada quando o hóspede
 * NÃO tem crédito aprovado de empresa, mas mesmo assim saiu sem pagar o
 * saldo devedor (com a promessa de pagar na próxima vinda).
 *
 * Diferente de:
 * - Vale: o HOTEL deve ao funcionário (adiantamento salarial).
 * - Conta a Receber por empresa: a EMPRESA deve ao hotel (crédito aprovado).
 * - DividaCliente: o HÓSPEDE deve ao hotel, sem cobertura de empresa.
 */
@Entity
@Table(name = "dividas_cliente")
public class DividaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 500)
    private String motivo;

    @Column(name = "reserva_origem_id")
    private Long reservaOrigemId;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(name = "data_quitacao")
    private LocalDateTime dataQuitacao;

    @Column(nullable = false, length = 20)
    private StatusDivida status = StatusDivida.PENDENTE;

    @Column(name = "registrado_por", length = 100)
    private String registradoPor;

    public enum StatusDivida {
        PENDENTE,
        QUITADA
    }

    // ── GETTERS E SETTERS ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Long getReservaOrigemId() { return reservaOrigemId; }
    public void setReservaOrigemId(Long reservaOrigemId) { this.reservaOrigemId = reservaOrigemId; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    public LocalDateTime getDataQuitacao() { return dataQuitacao; }
    public void setDataQuitacao(LocalDateTime dataQuitacao) { this.dataQuitacao = dataQuitacao; }

    public StatusDivida getStatus() { return status; }
    public void setStatus(StatusDivida status) { this.status = status; }

    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }
}