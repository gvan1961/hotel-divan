package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permissoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permissao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da permissão é obrigatório")
    @Column(unique = true, nullable = false, length = 100)
    private String nome; // RESERVA_CREATE, RESERVA_READ, RESERVA_UPDATE, etc.
    
    @Column(length = 200)
    private String descricao;
    
    @Column(length = 50)
    private String categoria; // RESERVAS, CLIENTES, PRODUTOS, FINANCEIRO, etc.
    
    @ManyToMany(mappedBy = "permissoes")
    @JsonIgnoreProperties("permissoes")
    private List<Perfil> perfis = new ArrayList<>();
    
    @ManyToMany(mappedBy = "permissoes")
    @JsonIgnoreProperties({"perfis", "permissoes"})
    private List<Usuario> usuarios = new ArrayList<>();
}
