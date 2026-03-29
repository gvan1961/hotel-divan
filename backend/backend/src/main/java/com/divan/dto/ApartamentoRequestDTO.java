package com.divan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartamentoRequestDTO {
    
    @NotBlank(message = "Número do apartamento é obrigatório")
    private String numeroApartamento;
    
    @NotNull(message = "Tipo de apartamento é obrigatório")
    private Long tipoApartamentoId;
    
    @NotNull(message = "Capacidade é obrigatória")
    @Min(value = 1, message = "Capacidade deve ser no mínimo 1")
    private Integer capacidade;
    
    // ✅ CORRIGIDO: String usa @NotBlank, não @Min
    @NotBlank(message = "Descrição das camas é obrigatória")
    private String camasDoApartamento;
    
    // ✅ TV é opcional, sem validação
    private String tv;
}
