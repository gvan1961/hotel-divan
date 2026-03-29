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
public class DiariaRequestDTO {
    
    @NotNull(message = "Tipo de apartamento é obrigatório")
    private Long tipoApartamentoId;
    
    @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
    @NotNull(message = "Quantidade de hóspedes é obrigatória")
    private Integer quantidade;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;
}
