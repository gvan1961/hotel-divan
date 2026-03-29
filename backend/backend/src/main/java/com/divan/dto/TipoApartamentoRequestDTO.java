package com.divan.dto;

import com.divan.entity.TipoApartamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoApartamentoRequestDTO {
    
    @NotNull(message = "Tipo é obrigatório")
    private TipoApartamento.TipoEnum tipo;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
}
