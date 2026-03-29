package com.divan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtratoDetalhadoDTO {
    
    // Informações da Reserva
    private Long reservaId;
    private String numeroApartamento;
    private String tipoApartamento;
    private String clienteNome;
    private String clienteCpf;
    private Integer quantidadeHospede;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private Integer quantidadeDiarias;
    private String statusReserva;
    
    // Histórico de Hóspedes
    private List<HistoricoHospedeDTO> historicoHospedes;
    
    // Lançamentos (Extratos)
    private List<LancamentoDTO> lancamentos;
    
    // Totais
    private BigDecimal totalDiarias;
    private BigDecimal totalProdutos;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal desconto;
    private BigDecimal saldoDevedor;
}
