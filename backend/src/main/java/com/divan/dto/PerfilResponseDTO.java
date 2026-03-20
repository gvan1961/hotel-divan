package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponseDTO {
    
    private Long id;
    private String nome;
    private String descricao;
    private List<PermissaoResponseDTO> permissoes;
}
