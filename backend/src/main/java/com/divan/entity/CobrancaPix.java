package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cobranca_pix")
public class CobrancaPix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private String correlationId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "br_code", length = 1000)
    private String brCode;

    @Column(name = "qr_code_image", length = 500)
    private String qrCodeImage;

    private String comentario;

    @Column(name = "reserva_id")
    private Long reservaId;

    @Enumerated(EnumType.STRING)
    private StatusPixEnum status = StatusPixEnum.PENDENTE;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "itens_json", columnDefinition = "TEXT")
    private String itensJson;

    public enum StatusPixEnum { PENDENTE, PAGO, EXPIRADO, CANCELADO }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getBrCode() { return brCode; }
    public void setBrCode(String brCode) { this.brCode = brCode; }
    public String getQrCodeImage() { return qrCodeImage; }
    public void setQrCodeImage(String qrCodeImage) { this.qrCodeImage = qrCodeImage; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }
    public StatusPixEnum getStatus() { return status; }
    public void setStatus(StatusPixEnum status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    
	public String getItensJson() {
		return itensJson;
	}
	public void setItensJson(String itensJson) {
		this.itensJson = itensJson;
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
		CobrancaPix other = (CobrancaPix) obj;
		return Objects.equals(id, other.id);
	}
    
    
}
