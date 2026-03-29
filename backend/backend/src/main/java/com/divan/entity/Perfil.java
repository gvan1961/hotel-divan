package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "perfis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do perfil é obrigatório")
    @Column(unique = true, nullable = false, length = 50)
    private String nome; // ADMIN, RECEPCIONISTA, GERENTE, VENDEDOR
    
    @Column(length = 200)
    private String descricao;
    
    @ManyToMany(fetch = FetchType.EAGER)    
    @JoinTable(
        name = "perfil_permissoes",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    @JsonIgnoreProperties("perfis")
   // private List<Permissao> permissoes = new ArrayList<>();
    private Set<Permissao> permissoes = new HashSet<>();
    
    @ManyToMany(mappedBy = "perfis")
    @JsonIgnoreProperties({"perfis", "permissoes"})
    private List<Usuario> usuarios = new ArrayList<>();
}
