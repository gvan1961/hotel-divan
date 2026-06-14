package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumo completo do histórico de um cliente:
 * estatísticas + lista de hospedagens.
 */
public class ResumoClienteDTO {

    // Dados do cliente
    private Long clienteId;
    private String nomeCliente;
    private String cpf;
    private String celular;

    // Estatísticas
    private int totalHospedagens;           // quantas vezes se hospedou
    private int totalDiasHospedado;         // soma de todas as diárias
    private BigDecimal totalGasto;          // soma de totalHospedagem das reservas
    private LocalDateTime primeiraHospedagem;
    private LocalDateTime ultimaHospedagem;
    private long diasDesdeUltimaHospedagem; // dias desde o último checkout
    private double mediaEstadia;            // média de diárias por hospedagem

    // Lista detalhada
    private List<HistoricoClienteDTO> hospedagens;

    // Getters e Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public int getTotalHospedagens() { return totalHospedagens; }
    public void setTotalHospedagens(int totalHospedagens) { this.totalHospedagens = totalHospedagens; }

    public int getTotalDiasHospedado() { return totalDiasHospedado; }
    public void setTotalDiasHospedado(int totalDiasHospedado) { this.totalDiasHospedado = totalDiasHospedado; }

    public BigDecimal getTotalGasto() { return totalGasto; }
    public void setTotalGasto(BigDecimal totalGasto) { this.totalGasto = totalGasto; }

    public LocalDateTime getPrimeiraHospedagem() { return primeiraHospedagem; }
    public void setPrimeiraHospedagem(LocalDateTime primeiraHospedagem) { this.primeiraHospedagem = primeiraHospedagem; }

    public LocalDateTime getUltimaHospedagem() { return ultimaHospedagem; }
    public void setUltimaHospedagem(LocalDateTime ultimaHospedagem) { this.ultimaHospedagem = ultimaHospedagem; }

    public long getDiasDesdeUltimaHospedagem() { return diasDesdeUltimaHospedagem; }
    public void setDiasDesdeUltimaHospedagem(long diasDesdeUltimaHospedagem) { this.diasDesdeUltimaHospedagem = diasDesdeUltimaHospedagem; }

    public double getMediaEstadia() { return mediaEstadia; }
    public void setMediaEstadia(double mediaEstadia) { this.mediaEstadia = mediaEstadia; }

    public List<HistoricoClienteDTO> getHospedagens() { return hospedagens; }
    public void setHospedagens(List<HistoricoClienteDTO> hospedagens) { this.hospedagens = hospedagens; }
}
