package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Configuração de inscrições de webhooks
 * Define quais eventos devem disparar webhooks e para onde enviar
 */
@Entity
@Table(name = "webhook_subscriptions")
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome descritivo da inscrição
     * Exemplo: "WhatsApp - Confirmação de Reserva", "Slack - Alertas de Checkout"
     */
    @Column(nullable = false, length = 100)
    private String nome;

    /**
     * Tipo de evento que dispara este webhook
     * Exemplo: RESERVA_CRIADA, PAGAMENTO_RECEBIDO, CHECKOUT_VENCIDO
     */
    @Column(nullable = false, length = 100)
    private String evento;

    /**
     * URL de destino para enviar o webhook
     * Exemplo: https://api.whatsapp.com/send
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * Se o webhook está ativo ou não
     */
    @Column(nullable = false)
    private Boolean ativo = true;

    /**
     * Cabeçalhos HTTP personalizados (JSON)
     * Exemplo: {"Authorization": "Bearer token123", "Content-Type": "application/json"}
     */
    @Column(columnDefinition = "TEXT")
    private String headers;

    /**
     * Secret para validar a assinatura do webhook
     * Usado para garantir que os webhooks recebidos são legítimos
     */
    @Column(length = 255)
    private String secret;

    /**
     * Método HTTP a ser usado (POST, PUT, etc)
     */
    @Column(length = 10)
    private String metodoHttp = "POST";

    /**
     * Timeout em segundos para a requisição
     */
    @Column
    private Integer timeoutSegundos = 30;

    /**
     * Número máximo de tentativas em caso de falha
     */
    @Column
    private Integer maxTentativas = 3;

    /**
     * Descrição/observações sobre este webhook
     */
    @Column(columnDefinition = "TEXT")
    private String descricao;

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

	public String getEvento() {
		return evento;
	}

	public void setEvento(String evento) {
		this.evento = evento;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getMetodoHttp() {
		return metodoHttp;
	}

	public void setMetodoHttp(String metodoHttp) {
		this.metodoHttp = metodoHttp;
	}

	public Integer getTimeoutSegundos() {
		return timeoutSegundos;
	}

	public void setTimeoutSegundos(Integer timeoutSegundos) {
		this.timeoutSegundos = timeoutSegundos;
	}

	public Integer getMaxTentativas() {
		return maxTentativas;
	}

	public void setMaxTentativas(Integer maxTentativas) {
		this.maxTentativas = maxTentativas;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
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
		WebhookSubscription other = (WebhookSubscription) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
