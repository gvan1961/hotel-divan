package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioFaturamentoDTO {
    
    private LocalDate dataInicio;
    private LocalDate dataFim;
    
    // Receitas
    private BigDecimal receitaDiarias;
    private BigDecimal receitaProdutos;
    private BigDecimal receitaTotal;
    
    // Pagamentos por forma
    private BigDecimal pagamentoDinheiro;
    private BigDecimal pagamentoPix;
    private BigDecimal pagamentoCartaoDebito;
    private BigDecimal pagamentoCartaoCredito;
    private BigDecimal pagamentoTransferencia;
    private BigDecimal pagamentoFaturado;
    
    // Estatísticas
    private Integer quantidadeReservas;
    private Integer quantidadeCheckIns;
    private Integer quantidadeCheckOuts;
    private Integer quantidadeCancelamentos;
    
    // Ticket Médio
    private BigDecimal ticketMedio;
    private BigDecimal mediaHospedes;
    private BigDecimal mediaDiarias;
}
