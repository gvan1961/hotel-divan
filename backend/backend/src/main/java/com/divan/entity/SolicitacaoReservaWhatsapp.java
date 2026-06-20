package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_reserva_whatsapp")
public class SolicitacaoReservaWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String cpf;

    @Column(name = "numero_whatsapp", nullable = false, length = 20)
    private String numeroWhatsapp;

    @Column(name = "data_checkin", nullable = false)
    private LocalDate dataCheckin;

    @Column(name = "data_checkout", nullable = false)
    private LocalDate dataCheckout;

    @Column(name = "quantidade_hospedes", nullable = false)
    private Integer quantidadeHospedes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status = StatusSolicitacao.PENDENTE;

    @Column(name = "data_solicitacao")
    private LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_visualizacao")
    private LocalDateTime dataVisualizacao;

    public enum StatusSolicitacao {
        PENDENTE, VISUALIZADA, ATENDIDA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNumeroWhatsapp() { return numeroWhatsapp; }
    public void setNumeroWhatsapp(String numeroWhatsapp) { this.numeroWhatsapp = numeroWhatsapp; }
    public LocalDate getDataCheckin() { return dataCheckin; }
    public void setDataCheckin(LocalDate dataCheckin) { this.dataCheckin = dataCheckin; }
    public LocalDate getDataCheckout() { return dataCheckout; }
    public void setDataCheckout(LocalDate dataCheckout) { this.dataCheckout = dataCheckout; }
    public Integer getQuantidadeHospedes() { return quantidadeHospedes; }
    public void setQuantidadeHospedes(Integer quantidadeHospedes) { this.quantidadeHospedes = quantidadeHospedes; }
    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    public LocalDateTime getDataVisualizacao() { return dataVisualizacao; }
    public void setDataVisualizacao(LocalDateTime dataVisualizacao) { this.dataVisualizacao = dataVisualizacao; }
}
