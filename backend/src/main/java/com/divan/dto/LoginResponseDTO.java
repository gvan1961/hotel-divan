package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String username;
    private String nome;
    private String email;
    private List<String> perfis;
    private List<String> permissoes;
}
