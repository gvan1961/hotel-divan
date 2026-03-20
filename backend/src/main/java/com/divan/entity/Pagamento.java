package com.divan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Reserva é obrigatória")
    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnoreProperties({"extratos", "historicos", "notasVenda", "apartamento", "cliente", "diaria"})
    private Reserva reserva;
    
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamentoEnum formaPagamento;
    
    @NotNull(message = "Data/hora do pagamento é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHoraPagamento;
    
    @Column(length = 200)
    private String observacao;
    
    public enum FormaPagamentoEnum {
        ESCOLHA, DINHEIRO, PIX, CARTAO_DEBITO, CARTAO_CREDITO, TRANSFERENCIA_BANCARIA, FATURADO
    }
}
