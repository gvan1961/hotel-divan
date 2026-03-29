package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_hospedes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoHospede {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Reserva é obrigatória")
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnore
    private Reserva reserva;
    
    @NotNull(message = "Data/hora é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHora;
    
    // CORREÇÃO: Permitir 0 para check-in inicial
    @Min(value = 0, message = "Quantidade anterior não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidadeAnterior;
    
    @Min(value = 1, message = "Quantidade nova deve ser no mínimo 1")
    @Column(nullable = false)
    private Integer quantidadeNova;
    
    @Column(length = 200)
    private String motivo;
}
