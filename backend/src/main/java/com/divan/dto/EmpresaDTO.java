package com.divan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CNPJ;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDTO {
    
    private Long id;
    
    @NotBlank(message = "Nome da empresa é obrigatório")
    private String nomeEmpresa;
    
    @CNPJ(message = "CNPJ inválido")
    private String cnpj;
    
    @NotBlank(message = "Contato é obrigatório")
    private String contato;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Celular deve estar no formato (XX) XXXXX-XXXX")
    private String celular;
}
