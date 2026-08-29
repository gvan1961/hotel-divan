package com.divan.dto;
 
import com.divan.entity.Reserva;
import java.time.LocalDateTime;
 
public class ReservaResumoDTO {
 
    private Long id;
    private ApartamentoSimples apartamento;
    private ClienteSimples cliente;
    private Reserva.StatusReservaEnum status;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public ApartamentoSimples getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoSimples apartamento) { this.apartamento = apartamento; }
 
    public ClienteSimples getCliente() { return cliente; }
    public void setCliente(ClienteSimples cliente) { this.cliente = cliente; }
 
    public Reserva.StatusReservaEnum getStatus() { return status; }
    public void setStatus(Reserva.StatusReservaEnum status) { this.status = status; }
 
    public LocalDateTime getDataCheckin() { return dataCheckin; }
    public void setDataCheckin(LocalDateTime dataCheckin) { this.dataCheckin = dataCheckin; }
 
    public LocalDateTime getDataCheckout() { return dataCheckout; }
    public void setDataCheckout(LocalDateTime dataCheckout) { this.dataCheckout = dataCheckout; }
 
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
