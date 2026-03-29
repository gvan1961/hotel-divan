package com.divan.dto;

import com.divan.entity.Apartamento;
import com.divan.entity.TipoApartamento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartamentoDTO {
    
    private Long id;
    
    @NotBlank(message = "Número do apartamento é obrigatório")
    private String numeroApartamento;
    
    @NotNull(message = "Tipo de apartamento é obrigatório")
    private Long tipoApartamentoId;
    
    private TipoApartamento.TipoEnum tipoApartamento;
    
    @Min(value = 1, message = "Capacidade deve ser no mínimo 1")
    private Integer capacidade;
    
    @NotBlank(message = "Camas do apartamento é obrigatório")
    private String camasDoApartamento;
    
    private Apartamento.StatusEnum status;
    private String tv;
}
