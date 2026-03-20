package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioOcupacaoDTO {
    
    private LocalDate data;
    private Integer totalApartamentos;
    private Integer apartamentosOcupados;
    private Integer apartamentosDisponiveis;
    private Integer apartamentosLimpeza;
    private Integer apartamentosManutencao;
    private Double taxaOcupacao; // Percentual
    private Integer totalHospedes;
    private BigDecimal receitaDiaria;
}
