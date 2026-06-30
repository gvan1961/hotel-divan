package com.divan.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "vouchers_wifi")
public class VoucherWifi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "username", length = 20, nullable = false)
    private String username;

    @Column(name = "password", length = 20, nullable = false)
    private String password;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "cancelado", nullable = false)
    private Boolean cancelado = false;

    public VoucherWifi() {
    }

    public VoucherWifi(Reserva reserva, String username, String password) {
        this.reserva = reserva;
        this.username = username;
        this.password = password;
        this.dataGeracao = LocalDateTime.now();
        this.cancelado = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDateTime dataGeracao) { this.dataGeracao = dataGeracao; }

    public LocalDateTime getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDateTime dataCancelamento) { this.dataCancelamento = dataCancelamento; }

    public Boolean getCancelado() { return cancelado; }
    public void setCancelado(Boolean cancelado) { this.cancelado = cancelado; }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        VoucherWifi other = (VoucherWifi) obj;
        return Objects.equals(id, other.id);
    }
}
