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
    private String nome;
    
    @Column(length = 200)
    private String descricao;
    
    @Column(length = 50)
    private String categoria;
    
    @ManyToMany(mappedBy = "permissoes")
    @JsonIgnoreProperties("permissoes")
    private List<Perfil> perfis = new ArrayList<>();
    
    @ManyToMany(mappedBy = "permissoes")
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
    
    public String getCategoria() {
        return categoria;
    }
    
    public List<Perfil> getPerfis() {
        return perfis;
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
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public void setPerfis(List<Perfil> perfis) {
        this.perfis = perfis;
    }
    
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }       
           
}
