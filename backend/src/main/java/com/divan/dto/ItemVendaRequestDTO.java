package com.divan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVendaRequestDTO {
    
    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;
    
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @NotNull(message = "Quantidade é obrigatória")
    private Integer quantidade;
    private String observacao;
}

