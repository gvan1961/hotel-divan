package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro de webhooks enviados
 * Armazena histórico completo de todos os webhooks disparados
 */
@Entity
@Table(name = "webhooks")
public class Webhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private WebhookSubscription subscription;

    @Column(nullable = false, length = 100)
    private String evento;

    @Column(nullable = false, length = 500)
    private String urlDestino;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusWebhookEnum status = StatusWebhookEnum.PENDING;

    @Column(nullable = false)
    private Integer tentativas = 0;

    @Column(nullable = false)
    private Integer maxTentativas = 3;

    @Column(name = "ultima_tentativa")
    private LocalDateTime ultimaTentativa;

    @Column(name = "proxima_tentativa")
    private LocalDateTime proximaTentativa;

    @Column(name = "resposta_http")
    private Integer respostaHttp;

    @Column(name = "resposta_body", columnDefinition = "TEXT")
    private String respostaBody;

    @Column(name = "erro_mensagem", columnDefinition = "TEXT")
    private String erroMensagem;

    @Column(name = "tempo_resposta_ms")
    private Long tempoRespostaMs;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(name = "entidade_tipo", length = 50)
    private String entidadeTipo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    // ════════════════════════════════════════════════════════════
    // GETTERS E SETTERS
    // ════════════════════════════════════════════════════════════

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WebhookSubscription getSubscription() {
        return subscription;
    }

    public void setSubscription(WebhookSubscription subscription) {
        this.subscription = subscription;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getUrlDestino() {
        return urlDestino;
    }

    public void setUrlDestino(String urlDestino) {
        this.urlDestino = urlDestino;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public StatusWebhookEnum getStatus() {
        return status;
    }

    public void setStatus(StatusWebhookEnum status) {
        this.status = status;
    }

    public Integer getTentativas() {
        return tentativas;
    }

    public void setTentativas(Integer tentativas) {
        this.tentativas = tentativas;
    }

    public Integer getMaxTentativas() {
        return maxTentativas;
    }

    public void setMaxTentativas(Integer maxTentativas) {
        this.maxTentativas = maxTentativas;
    }

    public LocalDateTime getUltimaTentativa() {
        return ultimaTentativa;
    }

    public void setUltimaTentativa(LocalDateTime ultimaTentativa) {
        this.ultimaTentativa = ultimaTentativa;
    }

    public LocalDateTime getProximaTentativa() {
        return proximaTentativa;
    }

    public void setProximaTentativa(LocalDateTime proximaTentativa) {
        this.proximaTentativa = proximaTentativa;
    }

    public Integer getRespostaHttp() {
        return respostaHttp;
    }

    public void setRespostaHttp(Integer respostaHttp) {
        this.respostaHttp = respostaHttp;
    }

    public String getRespostaBody() {
        return respostaBody;
    }

    public void setRespostaBody(String respostaBody) {
        this.respostaBody = respostaBody;
    }

    public String getErroMensagem() {
        return erroMensagem;
    }

    public void setErroMensagem(String erroMensagem) {
        this.erroMensagem = erroMensagem;
    }

    public Long getTempoRespostaMs() {
        return tempoRespostaMs;
    }

    public void setTempoRespostaMs(Long tempoRespostaMs) {
        this.tempoRespostaMs = tempoRespostaMs;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public void setEntidadeId(Long entidadeId) {
        this.entidadeId = entidadeId;
    }

    public String getEntidadeTipo() {
        return entidadeTipo;
    }

    public void setEntidadeTipo(String entidadeTipo) {
        this.entidadeTipo = entidadeTipo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    // ════════════════════════════════════════════════════════════
    // ENUM
    // ════════════════════════════════════════════════════════════

    public enum StatusWebhookEnum {
        PENDING,    // Aguardando envio
        SENDING,    // Enviando
        SUCCESS,    // Enviado com sucesso (HTTP 2xx)
        FAILED,     // Falhou após todas as tentativas
        CANCELLED   // Cancelado manualmente
    }
}