package com.divan.dto;

public class HospedeJantarDTO {
    
    private Long clienteId;
    private String nomeCliente;
    private String numeroApartamento;
    private Long reservaId;
    private String empresaNome;
    private Boolean empresaAutorizaTodos;
    private Boolean clienteAutorizado;
    private Boolean podeJantar;
    private Long hospedeId; // ID do HospedagemHospede
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUTORES
    // ═══════════════════════════════════════════════════════════
    
    public HospedeJantarDTO() {}
    
    public HospedeJantarDTO(
        Long clienteId,
        String nomeCliente,
        String numeroApartamento,
        Long reservaId,
        String empresaNome,
        Boolean empresaAutorizaTodos,
        Boolean clienteAutorizado,
        Long hospedeId
    ) {
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.numeroApartamento = numeroApartamento;
        this.reservaId = reservaId;
        this.empresaNome = empresaNome;
        this.empresaAutorizaTodos = empresaAutorizaTodos;
        this.clienteAutorizado = clienteAutorizado;
        this.hospedeId = hospedeId;
        
        // Calcular se pode jantar
        this.podeJantar = calcularPodeJantar();
    }
    
    // ═══════════════════════════════════════════════════════════
    // LÓGICA DE NEGÓCIO
    // ═══════════════════════════════════════════════════════════
    
    private Boolean calcularPodeJantar() {
        // REGRA 1: Se não tem empresa, pode jantar
        if (empresaNome == null || empresaNome.isEmpty()) {
            return true;
        }
        
        // REGRA 2: Se empresa autoriza todos, pode jantar
        if (empresaAutorizaTodos != null && empresaAutorizaTodos) {
            return true;
        }
        
        // REGRA 3: Verificar autorização individual
        return clienteAutorizado != null && clienteAutorizado;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS E SETTERS
    // ═══════════════════════════════════════════════════════════
    
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
    
    public String getEmpresaNome() {
        return empresaNome;
    }
    
    public void setEmpresaNome(String empresaNome) {
        this.empresaNome = empresaNome;
    }
    
    public Boolean getEmpresaAutorizaTodos() {
        return empresaAutorizaTodos;
    }
    
    public void setEmpresaAutorizaTodos(Boolean empresaAutorizaTodos) {
        this.empresaAutorizaTodos = empresaAutorizaTodos;
    }
    
    public Boolean getClienteAutorizado() {
        return clienteAutorizado;
    }
    
    public void setClienteAutorizado(Boolean clienteAutorizado) {
        this.clienteAutorizado = clienteAutorizado;
    }
    
    public Boolean getPodeJantar() {
        return podeJantar;
    }
    
    public void setPodeJantar(Boolean podeJantar) {
        this.podeJantar = podeJantar;
    }
    
    public Long getHospedeId() {
        return hospedeId;
    }
    
    public void setHospedeId(Long hospedeId) {
        this.hospedeId = hospedeId;
    }
    
    // ═══════════════════════════════════════════════════════════
    // TO STRING (para debug)
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public String toString() {
        return "HospedeJantarDTO{" +
                "clienteId=" + clienteId +
                ", nomeCliente='" + nomeCliente + '\'' +
                ", numeroApartamento='" + numeroApartamento + '\'' +
                ", empresaNome='" + empresaNome + '\'' +
                ", podeJantar=" + podeJantar +
                '}';
    }
}
