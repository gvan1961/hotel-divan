package com.divan.dto;

import com.divan.entity.ExtratoReserva;
import com.divan.entity.HistoricoHospede;
import com.divan.entity.Reserva;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {
    
    private Long id;
    private ClienteResponseDTO cliente;
    private ApartamentoResponseDTO apartamento;      
    
    
    private Integer quantidadeHospede;
    private LocalDateTime checkin;
    private LocalDateTime checkout;
    
    private Integer quantidadeDiarias;
    private BigDecimal valorDiaria;
    private BigDecimal totalDiarias;
    private BigDecimal totalHospedagem;
    private BigDecimal totalRecebido;
    private BigDecimal totalApagar;
    
    private Reserva.StatusReservaEnum status;
    private String observacoes;
    
    
    private LocalDateTime dataCheckin;
    private LocalDateTime dataCheckout;
    private Integer quantidadeDiaria;
    
    private BigDecimal totalDiaria;
    
    private List<ExtratoReserva> extratos;
    private List<HistoricoHospede> historicos;
    
    
    
    
}
