package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tipos_apartamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoApartamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEnum tipo;
    
    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false, length = 200)
    private String descricao;
    
    @OneToMany(mappedBy = "tipoApartamento", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("tipoApartamento")
    private List<Apartamento> apartamentos;
    
    @OneToMany(mappedBy = "tipoApartamento", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("tipoApartamento")
    private List<Diaria> diarias;
    
    public enum TipoEnum {
        A, B, C, D
    }
}
