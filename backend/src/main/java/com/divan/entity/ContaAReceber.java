package com.divan.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contas_receber")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaAReceber {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Reserva é obrigatória")
    @OneToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @DecimalMin(value = "0.0", message = "Valor pago não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo;
    
    @NotNull(message = "Data de vencimento é obrigatória")
    @Column(nullable = false)
    private LocalDate dataVencimento;
    
    private LocalDate dataPagamento;
    
    private LocalDateTime dataCriacao;
    private String observacao; // Mais detalhes
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusContaEnum status = StatusContaEnum.EM_ABERTO;
    
    @NotBlank(message = "Descrição é obrigatória")
    @Column(nullable = false, length = 200)
    private String descricao;
    
    public enum StatusContaEnum {
        EM_ABERTO, PAGA, VENCIDA, ATRASADA, PENDENTE, CANCELADA, PARCIAL
    }
}
