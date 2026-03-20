package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioProdutoDTO {
    
    private Long produtoId;
    private String nomeProduto;
    private String categoria;
    private Integer quantidadeVendida;
    private BigDecimal valorTotal;
    private BigDecimal valorMedio;
    private Integer quantidadeVendas;
}
