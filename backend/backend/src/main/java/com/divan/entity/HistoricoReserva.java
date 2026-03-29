package com.divan.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_reservas")
@Data
public class HistoricoReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(length = 100)
    private String tipo; // Ex: ALTERACAO_CHECKOUT, TRANSFERENCIA, CANCELAMENTO, etc.

    @Column(length = 1000)
    private String detalhes;

    @PrePersist
    protected void onCreate() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}