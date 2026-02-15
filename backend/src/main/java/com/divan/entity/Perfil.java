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
import java.util.Objects;
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
    private String nome;
    
    @Column(length = 200)
    private String descricao;
    
    @ManyToMany(fetch = FetchType.EAGER)    
    @JoinTable(
        name = "perfil_permissoes",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    @JsonIgnoreProperties("perfis")
    private Set<Permissao> permissoes = new HashSet<>();
    
    @ManyToMany(mappedBy = "perfis")
    @JsonIgnoreProperties({"perfis", "permissoes"})
    private List<Usuario> usuarios = new ArrayList<>();
    
    // ✅ GETTERS MANUAIS (caso Lombok não funcione)
    public Long getId() {
        return id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public Set<Permissao> getPermissoes() {
        return permissoes;
    }
    
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    
    // ✅ SETTERS MANUAIS
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public void setPermissoes(Set<Permissao> permissoes) {
        this.permissoes = permissoes;
    }
    
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }	  
    
}
