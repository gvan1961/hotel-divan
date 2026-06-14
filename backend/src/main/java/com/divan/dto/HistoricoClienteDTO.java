package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma hospedagem do histórico do cliente.
 */
public class HistoricoClienteDTO {

    private Long reservaId;
    private String numeroApartamento;
    private String tipoApartamento;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private LocalDateTime dataCheckoutReal;
    private Integer quantidadeDiarias;
    private Integer quantidadeHospedes;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private String status;          // ATIVA, FINALIZADA, CANCELADA
    private boolean titular;        // se foi titular da reserva

    // Getters e Setters
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    public String getNumeroApartamento() { return numeroApartamento; }
    public void setNumeroApartamento(String numeroApartamento) { this.numeroApartamento = numeroApartamento; }

    public String getTipoApartamento() { return tipoApartamento; }
    public void setTipoApartamento(String tipoApartamento) { this.tipoApartamento = tipoApartamento; }

    public LocalDateTime getDataCheckin() { return dataCheckin; }
    public void setDataCheckin(LocalDateTime dataCheckin) { this.dataCheckin = dataCheckin; }

    public LocalDateTime getDataCheckout() { return dataCheckout; }
    public void setDataCheckout(LocalDateTime dataCheckout) { this.dataCheckout = dataCheckout; }

    public LocalDateTime getDataCheckoutReal() { return dataCheckoutReal; }
    public void setDataCheckoutReal(LocalDateTime dataCheckoutReal) { this.dataCheckoutReal = dataCheckoutReal; }

    public Integer getQuantidadeDiarias() { return quantidadeDiarias; }
    public void setQuantidadeDiarias(Integer quantidadeDiarias) { this.quantidadeDiarias = quantidadeDiarias; }

    public Integer getQuantidadeHospedes() { return quantidadeHospedes; }
    public void setQuantidadeHospedes(Integer quantidadeHospedes) { this.quantidadeHospedes = quantidadeHospedes; }

    public BigDecimal getTotalHospedagem() { return totalHospedagem; }
    public void setTotalHospedagem(BigDecimal totalHospedagem) { this.totalHospedagem = totalHospedagem; }

    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public void setTotalRecebido(BigDecimal totalRecebido) { this.totalRecebido = totalRecebido; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isTitular() { return titular; }
    public void setTitular(boolean titular) { this.titular = titular; }
}
