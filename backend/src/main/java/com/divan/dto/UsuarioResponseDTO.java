package com.divan.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioResponseDTO {
    
    private Long id;
    private String nome;
    private String username;
    private String email;
    private Boolean ativo;
    private LocalDateTime dataCriacao;
    private LocalDateTime ultimoAcesso;
    private List<PerfilResponseDTO> perfis;
    private List<PermissaoResponseDTO> permissoes;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Boolean getAtivo() {
		return ativo;
	}
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}
	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}
	public LocalDateTime getUltimoAcesso() {
		return ultimoAcesso;
	}
	public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
		this.ultimoAcesso = ultimoAcesso;
	}
	public List<PerfilResponseDTO> getPerfis() {
		return perfis;
	}
	public void setPerfis(List<PerfilResponseDTO> perfis) {
		this.perfis = perfis;
	}
	public List<PermissaoResponseDTO> getPermissoes() {
		return permissoes;
	}
	public void setPermissoes(List<PermissaoResponseDTO> permissoes) {
		this.permissoes = permissoes;
	}   
    
}
