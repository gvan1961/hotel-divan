package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.List;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da empresa é obrigatório")
    @Column(nullable = false, length = 100)
    private String nomeEmpresa;
    
    @CNPJ(message = "CNPJ inválido")
    @Column(unique = true, nullable = false)
    private String cnpj;
    
    @NotBlank(message = "Contato é obrigatório")
    @Column(nullable = false, length = 100)
    private String contato;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Celular deve estar no formato (XX) XXXXX-XXXX")
    @Column(nullable = false)
    private String celular;
    
    // IMPORTANTE: JsonIgnoreProperties evita o loop infinito
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("empresa")
    private List<Cliente> clientes;
}