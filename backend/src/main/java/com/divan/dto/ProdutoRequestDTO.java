package com.divan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoRequestDTO {
    
    @NotBlank(message = "Nome do produto é obrigatório")
    private String nomeProduto;
    
    @NotNull(message = "Quantidade é obrigatória")
    @PositiveOrZero(message = "Quantidade deve ser maior ou igual a zero")
    private Integer quantidade;
    
    @NotNull(message = "Valor de venda é obrigatório")
    @PositiveOrZero(message = "Valor de venda deve ser maior ou igual a zero")
    private BigDecimal valorVenda;
    
    @NotNull(message = "Valor de compra é obrigatório")
    @PositiveOrZero(message = "Valor de compra deve ser maior ou igual a zero")
    private BigDecimal valorCompra;
    
    private Long categoriaId;  // ✅ APENAS O ID DA CATEGORIA
}
