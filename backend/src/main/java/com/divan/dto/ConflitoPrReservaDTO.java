package com.divan.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConflitoPrReservaDTO {
    
    // Informações do apartamento em conflito
    private Long apartamentoId;
    private String numeroApartamento;
    private String tipoApartamento;
    
    // Hóspede atual (checkout atrasado)
    private Long reservaAtualId;
    private String hospedeAtualNome;
    private LocalDateTime checkoutPrevisto;
    private Long horasAtraso;
    
    // Pré-reserva que está conflitando
    private Long preReservaId;
    private String clientePreReservaNome;
    private LocalDateTime dataCheckinPreReserva;
    
    // Apartamentos disponíveis para transferência
    private List<ApartamentoDisponivelDTO> apartamentosDisponiveis;
    
    // Status do conflito
    private String nivelGravidade;
    private String recomendacao;
    
    // Construtores
    public ConflitoPrReservaDTO() {}
    
    // Getters e Setters
    public Long getApartamentoId() {
        return apartamentoId;
    }
    
    public void setApartamentoId(Long apartamentoId) {
        this.apartamentoId = apartamentoId;
    }
    
    public String getNumeroApartamento() {
        return numeroApartamento;
    }
    
    public void setNumeroApartamento(String numeroApartamento) {
        this.numeroApartamento = numeroApartamento;
    }
    
    public String getTipoApartamento() {
        return tipoApartamento;
    }
    
    public void setTipoApartamento(String tipoApartamento) {
        this.tipoApartamento = tipoApartamento;
    }
    
    public Long getReservaAtualId() {
        return reservaAtualId;
    }
    
    public void setReservaAtualId(Long reservaAtualId) {
        this.reservaAtualId = reservaAtualId;
    }
    
    public String getHospedeAtualNome() {
        return hospedeAtualNome;
    }
    
    public void setHospedeAtualNome(String hospedeAtualNome) {
        this.hospedeAtualNome = hospedeAtualNome;
    }
    
    public LocalDateTime getCheckoutPrevisto() {
        return checkoutPrevisto;
    }
    
    public void setCheckoutPrevisto(LocalDateTime checkoutPrevisto) {
        this.checkoutPrevisto = checkoutPrevisto;
    }
    
    public Long getHorasAtraso() {
        return horasAtraso;
    }
    
    public void setHorasAtraso(Long horasAtraso) {
        this.horasAtraso = horasAtraso;
    }
    
    public Long getPreReservaId() {
        return preReservaId;
    }
    
    public void setPreReservaId(Long preReservaId) {
        this.preReservaId = preReservaId;
    }
    
    public String getClientePreReservaNome() {
        return clientePreReservaNome;
    }
    
    public void setClientePreReservaNome(String clientePreReservaNome) {
        this.clientePreReservaNome = clientePreReservaNome;
    }
    
    public LocalDateTime getDataCheckinPreReserva() {
        return dataCheckinPreReserva;
    }
    
    public void setDataCheckinPreReserva(LocalDateTime dataCheckinPreReserva) {
        this.dataCheckinPreReserva = dataCheckinPreReserva;
    }
    
    public List<ApartamentoDisponivelDTO> getApartamentosDisponiveis() {
        return apartamentosDisponiveis;
    }
    
    public void setApartamentosDisponiveis(List<ApartamentoDisponivelDTO> apartamentosDisponiveis) {
        this.apartamentosDisponiveis = apartamentosDisponiveis;
    }
    
    public String getNivelGravidade() {
        return nivelGravidade;
    }
    
    public void setNivelGravidade(String nivelGravidade) {
        this.nivelGravidade = nivelGravidade;
    }
    
    public String getRecomendacao() {
        return recomendacao;
    }
    
    public void setRecomendacao(String recomendacao) {
        this.recomendacao = recomendacao;
    }
    
    // Classe interna
    public static class ApartamentoDisponivelDTO {
        private Long apartamentoId;
        private String numeroApartamento;
        private String tipoApartamento;
        private String categoria;
        private Boolean recomendado;
        
        public ApartamentoDisponivelDTO() {}
        
        // Getters e Setters
        public Long getApartamentoId() {
            return apartamentoId;
        }
        
        public void setApartamentoId(Long apartamentoId) {
            this.apartamentoId = apartamentoId;
        }
        
        public String getNumeroApartamento() {
            return numeroApartamento;
        }
        
        public void setNumeroApartamento(String numeroApartamento) {
            this.numeroApartamento = numeroApartamento;
        }
        
        public String getTipoApartamento() {
            return tipoApartamento;
        }
        
        public void setTipoApartamento(String tipoApartamento) {
            this.tipoApartamento = tipoApartamento;
        }
        
        public String getCategoria() {
            return categoria;
        }
        
        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }
        
        public Boolean getRecomendado() {
            return recomendado;
        }
        
        public void setRecomendado(Boolean recomendado) {
            this.recomendado = recomendado;
        }
    }
}