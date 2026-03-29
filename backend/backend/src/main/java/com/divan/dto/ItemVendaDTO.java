package com.divan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVendaDTO {
    
    private Long id;
    
    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;
    
    private String produtoNome;
    
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    private Integer quantidade;
    
    @DecimalMin(value = "0.01", message = "Valor unitário deve ser maior que zero")
    private BigDecimal valorUnitario;
    
    @DecimalMin(value = "0.01", message = "Total do item deve ser maior que zero")
    private BigDecimal totalItem;
}
