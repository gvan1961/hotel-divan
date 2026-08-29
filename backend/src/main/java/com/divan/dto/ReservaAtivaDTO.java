package com.divan.dto;

public class ReservaAtivaDTO {

    private Long id;
    private ApartamentoSimples apartamento;
    private ClienteSimples cliente;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ApartamentoSimples getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoSimples apartamento) { this.apartamento = apartamento; }

    public ClienteSimples getCliente() { return cliente; }
    public void setCliente(ClienteSimples cliente) { this.cliente = cliente; }

    public static class ApartamentoSimples {
        private Long id;
        private String numeroApartamento;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNumeroApartamento() { return numeroApartamento; }
        public void setNumeroApartamento(String numeroApartamento) { this.numeroApartamento = numeroApartamento; }
    }

    public static class ClienteSimples {
        private Long id;
        private String nome;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
    }
}
