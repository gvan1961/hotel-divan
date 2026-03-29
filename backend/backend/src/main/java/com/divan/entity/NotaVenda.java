package com.divan.entity;

import com.divan.entity.Pagamento.FormaPagamentoEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notas_venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaVenda {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Data/hora da venda é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHoraVenda;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVendaEnum tipoVenda;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamentoEnum formaPagamento;
    
    @Column(name = "observacao", length = 500)
    private String observacao;
    
    @ManyToOne
    @JoinColumn(name = "reserva_id")
    @JsonIgnore
    private Reserva reserva;
    
    // CORREÇÃO: Mudar de 0.01 para 0.0 para permitir zero temporariamente
    @DecimalMin(value = "0.0", message = "Total não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @OneToMany(mappedBy = "notaVenda", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("notaVenda")
    private List<ItemVenda> itens;    
        
    public enum TipoVendaEnum {
        VISTA, APARTAMENTO, FATURADO
    }
    
    public enum Status {
        ABERTA, FECHADA, CANCELADA
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
    
    public enum FormaPagamentoEnum {
        DINHEIRO, PIX, CARTAO_DEBITO, CARTAO_CREDITO, TRANSFERENCIA_BANCARIA, FATURADO
    }
}
