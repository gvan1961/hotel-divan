package com.divan.dto;

import java.time.LocalDateTime;

public class VoucherWifiDTO {
    private Long id;
    private Long reservaId;
    private String numeroApartamento;
    private String codigo;
    private LocalDateTime dataGeracao;
    private Boolean cancelado;

    public VoucherWifiDTO(Long id, Long reservaId, String numeroApartamento,
                          String codigo, LocalDateTime dataGeracao, Boolean cancelado) {
        this.id = id;
        this.reservaId = reservaId;
        this.numeroApartamento = numeroApartamento;
        this.codigo = codigo;
        this.dataGeracao = dataGeracao;
        this.cancelado = cancelado;
    }

    public Long getId() { return id; }
    public Long getReservaId() { return reservaId; }
    public String getNumeroApartamento() { return numeroApartamento; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public Boolean getCancelado() { return cancelado; }
}
