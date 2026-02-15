package com.divan.dto;

import java.time.LocalTime;

public class TurnoDTO {
    
    private Long id;
    private String nome;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Boolean ativo;
    private Integer ordem;
    
    // GETTERS E SETTERS
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
    
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public LocalTime getHoraFim() {
        return horaFim;
    }
    
    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public Integer getOrdem() {
        return ordem;
    }
    
    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
