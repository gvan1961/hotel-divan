package com.divan.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "desconto_reserva")
public class DescontoReserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Column(length = 500)
    private String motivo;
    
    @Column(name = "data_hora_desconto", nullable = false)
    private LocalDateTime dataHoraDesconto;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Quem aplicou o desconto
    
    @ManyToOne
    @JoinColumn(name = "caixa_id")
    private FechamentoCaixa caixa; // Em qual caixa foi aplicado
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Reserva getReserva() {
        return reserva;
    }
    
    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public LocalDateTime getDataHoraDesconto() {
        return dataHoraDesconto;
    }
    
    public void setDataHoraDesconto(LocalDateTime dataHoraDesconto) {
        this.dataHoraDesconto = dataHoraDesconto;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public FechamentoCaixa getCaixa() {
        return caixa;
    }
    
    public void setCaixa(FechamentoCaixa caixa) {
        this.caixa = caixa;
    }
}
