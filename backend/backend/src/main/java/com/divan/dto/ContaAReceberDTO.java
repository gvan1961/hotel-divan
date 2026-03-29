package com.divan.dto;

import com.divan.entity.ContaAReceber.StatusContaEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaAReceberDTO {
    private Long id;
    private Long reservaId;
    private String clienteNome;
    private String empresaNome;
    private BigDecimal valor;
    private BigDecimal valorPago;
    private BigDecimal saldo;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private StatusContaEnum status;
    private String descricao;
    private Integer diasVencido;
    
    private String numeroApartamento;
    private Integer quantidadeHospede;
    private Integer quantidadeDiaria;
    private BigDecimal totalDiaria;
    private BigDecimal totalConsumo;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal desconto;
    private BigDecimal totalApagar;
}
