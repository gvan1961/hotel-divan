package com.divan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissaoRequestDTO {
    
    @NotBlank(message = "Nome da permissão é obrigatório")
    private String nome;
    
    private String descricao;
    
    private String categoria;
}
