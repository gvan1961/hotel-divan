package com.divan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaRequestDTO {
    
    @NotNull(message = "Apartamento é obrigatório")
    private Long apartamentoId;
    
    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;
    
    @Min(value = 1, message = "Quantidade de hóspedes deve ser no mínimo 1")
    @NotNull(message = "Quantidade de hóspedes é obrigatória")
    private Integer quantidadeHospede;
    
    @NotNull(message = "Data de check-in é obrigatória")
    private LocalDateTime dataCheckin;
    
    @NotNull(message = "Data de check-out é obrigatória")
    private LocalDateTime dataCheckout;
    
    // ✅ NOVO - Lista de hóspedes individuais
    private List<HospedeReservaDTO> hospedes = new ArrayList<>();
    
    // Getters e Setters
    public Long getApartamentoId() {
        return apartamentoId;
    }
    
    public void setApartamentoId(Long apartamentoId) {
        this.apartamentoId = apartamentoId;
    }
    
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public Integer getQuantidadeHospede() {
        return quantidadeHospede;
    }
    
    public void setQuantidadeHospede(Integer quantidadeHospede) {
        this.quantidadeHospede = quantidadeHospede;
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
    
    // ✅ NOVO - Getter/Setter para lista de hóspedes
    public List<HospedeReservaDTO> getHospedes() {
        return hospedes;
    }
    
    public void setHospedes(List<HospedeReservaDTO> hospedes) {
        this.hospedes = hospedes;
    }
}
