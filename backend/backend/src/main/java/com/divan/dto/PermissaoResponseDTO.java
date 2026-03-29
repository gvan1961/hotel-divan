package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissaoResponseDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    private String categoria;
}
