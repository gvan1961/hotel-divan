package com.divan.dto;

import com.divan.entity.Pagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagamentoResumoDTO {

    private Long id;
    private BigDecimal valor;
    private Pagamento.FormaPagamentoEnum formaPagamento;
    private Pagamento.TipoPagamentoEnum tipo;
    private LocalDateTime dataHoraPagamento;
    private Long reservaId;
    private String apartamentoNumero;
    private String clienteNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Pagamento.FormaPagamentoEnum getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(Pagamento.FormaPagamentoEnum formaPagamento) { this.formaPagamento = formaPagamento; }

    public Pagamento.TipoPagamentoEnum getTipo() { return tipo; }
    public void setTipo(Pagamento.TipoPagamentoEnum tipo) { this.tipo = tipo; }

    public LocalDateTime getDataHoraPagamento() { return dataHoraPagamento; }
    public void setDataHoraPagamento(LocalDateTime dataHoraPagamento) { this.dataHoraPagamento = dataHoraPagamento; }

    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    public String getApartamentoNumero() { return apartamentoNumero; }
    public void setApartamentoNumero(String apartamentoNumero) { this.apartamentoNumero = apartamentoNumero; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
}
