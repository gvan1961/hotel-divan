package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Reserva é obrigatória")
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnoreProperties({"extratos", "historicos", "notasVenda", "apartamento", "cliente", "diaria"})
    private Reserva reserva;

    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamentoEnum formaPagamento;

    @NotNull(message = "Data/hora do pagamento é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHoraPagamento;

    @Column(length = 200)
    private String observacao;

    // ✅ NOVO — tipo do pagamento (PAGAMENTO ou ADIANTAMENTO)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoPagamentoEnum tipo = TipoPagamentoEnum.PAGAMENTO;

    public enum FormaPagamentoEnum {
        ESCOLHA, DINHEIRO, PIX, CARTAO_DEBITO, CARTAO_CREDITO, 
        TRANSFERENCIA_BANCARIA, FATURADO, LINK_PIX, LINK_CARTAO, DEBITO_EM_CONTA  
    }

    // ✅ NOVO ENUM
    public enum TipoPagamentoEnum {
        PAGAMENTO,    // pagamento normal (abate saldo devedor da reserva)
        ADIANTAMENTO  // crédito do hóspede (abate consumos futuros)
    }

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

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public FormaPagamentoEnum getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamentoEnum formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDateTime getDataHoraPagamento() {
        return dataHoraPagamento;
    }

    public void setDataHoraPagamento(LocalDateTime dataHoraPagamento) {
        this.dataHoraPagamento = dataHoraPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    // ✅ NOVOS getter/setter
    public TipoPagamentoEnum getTipo() {
        return tipo != null ? tipo : TipoPagamentoEnum.PAGAMENTO;
    }

    public void setTipo(TipoPagamentoEnum tipo) {
        this.tipo = tipo;
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
        Pagamento other = (Pagamento) obj;
        return Objects.equals(id, other.id);
    }
}