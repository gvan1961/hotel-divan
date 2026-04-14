package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "sorteios")
public class Sorteio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    private LocalDate dataSorteio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEnum status = StatusEnum.ATIVA;

    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    public enum StatusEnum {
        ATIVA, ENCERRADA, REALIZADA
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalDate getDataSorteio() { return dataSorteio; }
    public void setDataSorteio(LocalDate dataSorteio) { this.dataSorteio = dataSorteio; }
    public StatusEnum getStatus() { return status; }
    public void setStatus(StatusEnum status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sorteio other = (Sorteio) obj;
		return Objects.equals(id, other.id);
	}

    
}
