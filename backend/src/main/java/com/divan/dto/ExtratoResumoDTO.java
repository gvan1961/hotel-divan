package com.divan.dto;

import com.divan.entity.ExtratoReserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExtratoResumoDTO {

    private Long id;
    private LocalDateTime dataHoraLancamento;
    private String descricao;
    private ExtratoReserva.StatusLancamentoEnum statusLancamento;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal totalLancamento;
    private Long notaVendaId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataHoraLancamento() { return dataHoraLancamento; }
    public void setDataHoraLancamento(LocalDateTime dataHoraLancamento) { this.dataHoraLancamento = dataHoraLancamento; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public ExtratoReserva.StatusLancamentoEnum getStatusLancamento() { return statusLancamento; }
    public void setStatusLancamento(ExtratoReserva.StatusLancamentoEnum statusLancamento) { this.statusLancamento = statusLancamento; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }

    public BigDecimal getTotalLancamento() { return totalLancamento; }
    public void setTotalLancamento(BigDecimal totalLancamento) { this.totalLancamento = totalLancamento; }

    public Long getNotaVendaId() { return notaVendaId; }
    public void setNotaVendaId(Long notaVendaId) { this.notaVendaId = notaVendaId; }
}
