package com.divan.dto;

import java.time.LocalDateTime;

public class ValidarHospedeDTO {
    
    private Long clienteId;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    
    // Construtores
    public ValidarHospedeDTO() {}
    
    public ValidarHospedeDTO(Long clienteId, LocalDateTime dataCheckin, LocalDateTime dataCheckout) {
        this.clienteId = clienteId;
        this.dataCheckin = dataCheckin;
        this.dataCheckout = dataCheckout;
    }
    
    // Getters e Setters
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public LocalDateTime getDataCheckin() {
        return dataCheckin;
    }
    
    public void setDataCheckin(LocalDateTime dataCheckin) {
        this.dataCheckin = dataCheckin;
    }
    
    public LocalDateTime getDataCheckout() {
        return dataCheckout;
    }
    
    public void setDataCheckout(LocalDateTime dataCheckout) {
        this.dataCheckout = dataCheckout;
    }
    
    @Override
    public String toString() {
        return "ValidarHospedeDTO{" +
                "clienteId=" + clienteId +
                ", dataCheckin=" + dataCheckin +
                ", dataCheckout=" + dataCheckout +
                '}';
    }
}