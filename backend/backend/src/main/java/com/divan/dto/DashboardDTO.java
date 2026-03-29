package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    
    private LocalDate data;
    
    // Ocupação Hoje
    private Integer apartamentosOcupados;
    private Integer apartamentosDisponiveis;
    private Double taxaOcupacao;
    private Integer totalHospedes;
    
    // Financeiro Hoje
    private BigDecimal receitaDia;
    private BigDecimal receitaMes;
    private BigDecimal pagamentosRecebidos;
    private BigDecimal contasReceber;
    
    // Movimentação
    private Integer checkInsHoje;
    private Integer checkOutsHoje;
    private Integer reservasAtivas;
    
    // Produtos
    private Integer produtosSemEstoque;
    private Integer produtosEstoqueBaixo;
    
    // Gráficos
    private List<RelatorioOcupacaoDTO> ocupacaoUltimos7Dias;
    private List<RelatorioProdutoDTO> top5ProdutosMaisVendidos;
}
