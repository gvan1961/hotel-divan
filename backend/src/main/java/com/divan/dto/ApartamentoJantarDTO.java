package com.divan.dto;

import java.util.ArrayList;
import java.util.List;

public class ApartamentoJantarDTO {
    
    private String numeroApartamento;
    private Long reservaId;
    private List<HospedeJantarInfoDTO> hospedes;
    private Integer totalHospedes;
    
    // ═══════════════════════════════════════════════════════════
    // CLASSE INTERNA: Info do Hóspede
    // ═══════════════════════════════════════════════════════════
    public static class HospedeJantarInfoDTO {
        private Long clienteId;
        private String nomeCliente;
        private String empresaNome;
        private Boolean titular;
        
        public HospedeJantarInfoDTO() {}
        
        public HospedeJantarInfoDTO(Long clienteId, String nomeCliente, String empresaNome, Boolean titular) {
            this.clienteId = clienteId;
            this.nomeCliente = nomeCliente;
            this.empresaNome = empresaNome;
            this.titular = titular;
        }
        
        // Getters e Setters
        public Long getClienteId() {
            return clienteId;
        }
        
        public void setClienteId(Long clienteId) {
            this.clienteId = clienteId;
        }
        
        public String getNomeCliente() {
            return nomeCliente;
        }
        
        public void setNomeCliente(String nomeCliente) {
            this.nomeCliente = nomeCliente;
        }
        
        public String getEmpresaNome() {
            return empresaNome;
        }
        
        public void setEmpresaNome(String empresaNome) {
            this.empresaNome = empresaNome;
        }
        
        public Boolean getTitular() {
            return titular;
        }
        
        public void setTitular(Boolean titular) {
            this.titular = titular;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUTORES
    // ═══════════════════════════════════════════════════════════
    
    public ApartamentoJantarDTO() {
        this.hospedes = new ArrayList<>();
    }
    
    public ApartamentoJantarDTO(String numeroApartamento, Long reservaId) {
        this.numeroApartamento = numeroApartamento;
        this.reservaId = reservaId;
        this.hospedes = new ArrayList<>();
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════
    
    public void adicionarHospede(Long clienteId, String nomeCliente, String empresaNome, Boolean titular) {
        this.hospedes.add(new HospedeJantarInfoDTO(clienteId, nomeCliente, empresaNome, titular));
        this.totalHospedes = this.hospedes.size();
    }
    
    public String getNomesHospedes() {
        if (hospedes == null || hospedes.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hospedes.size(); i++) {
            sb.append(hospedes.get(i).getNomeCliente());
            if (i < hospedes.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS E SETTERS
    // ═══════════════════════════════════════════════════════════
    
    public String getNumeroApartamento() {
        return numeroApartamento;
    }
    
    public void setNumeroApartamento(String numeroApartamento) {
        this.numeroApartamento = numeroApartamento;
    }
    
    public Long getReservaId() {
        return reservaId;
    }
    
    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }
    
    public List<HospedeJantarInfoDTO> getHospedes() {
        return hospedes;
    }
    
    public void setHospedes(List<HospedeJantarInfoDTO> hospedes) {
        this.hospedes = hospedes;
        this.totalHospedes = hospedes != null ? hospedes.size() : 0;
    }
    
    public Integer getTotalHospedes() {
        return totalHospedes;
    }
    
    public void setTotalHospedes(Integer totalHospedes) {
        this.totalHospedes = totalHospedes;
    }
    
    // ═══════════════════════════════════════════════════════════
    // TO STRING
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public String toString() {
        return "ApartamentoJantarDTO{" +
                "numeroApartamento='" + numeroApartamento + '\'' +
                ", totalHospedes=" + totalHospedes +
                ", hospedes=" + getNomesHospedes() +
                '}';
    }
}
