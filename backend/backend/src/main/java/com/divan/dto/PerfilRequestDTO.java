package com.divan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRequestDTO {
    
    @NotBlank(message = "Nome do perfil é obrigatório")
    private String nome;
    
    private String descricao;
    
    private List<Long> permissoesIds;
}
