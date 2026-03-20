package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumoPagamentosDTO {
    
    private BigDecimal totalDinheiro;
    private BigDecimal totalPix;
    private BigDecimal totalCartaoDebito;
    private BigDecimal totalCartaoCredito;
    private BigDecimal totalTransferencia;
    private BigDecimal totalFaturado;
    private BigDecimal totalGeral;
    private Integer quantidadePagamentos;
}
