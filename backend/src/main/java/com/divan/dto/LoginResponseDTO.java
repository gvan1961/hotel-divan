package com.divan.dto;

import java.util.List;


public class LoginResponseDTO {
    
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String username;
    private String nome;
    private String email;
    private List<String> perfis;
    private List<String> permissoes;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<String> getPerfis() {
		return perfis;
	}
	public void setPerfis(List<String> perfis) {
		this.perfis = perfis;
	}
	public List<String> getPermissoes() {
		return permissoes;
	}
	public void setPermissoes(List<String> permissoes) {
		this.permissoes = permissoes;
	}
    
    
}
