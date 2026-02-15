package com.divan.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaDetalhadaDTO {
    
    private Long notaVendaId;
    private LocalDateTime dataHora;
    private BigDecimal valorTotal;
    private String tipoVenda;
    private List<ProdutoVendidoDTO> produtos = new ArrayList<>();
    
    public VendaDetalhadaDTO() {}
    
    public VendaDetalhadaDTO(Long notaVendaId, LocalDateTime dataHora, 
                            BigDecimal valorTotal, String tipoVenda) {
        this.notaVendaId = notaVendaId;
        this.dataHora = dataHora;
        this.valorTotal = valorTotal;
        this.tipoVenda = tipoVenda;
    }
    
    public Long getNotaVendaId() { return notaVendaId; }
    public void setNotaVendaId(Long notaVendaId) { this.notaVendaId = notaVendaId; }
    
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    
    public String getTipoVenda() { return tipoVenda; }
    public void setTipoVenda(String tipoVenda) { this.tipoVenda = tipoVenda; }
    
    public List<ProdutoVendidoDTO> getProdutos() { return produtos; }
    public void setProdutos(List<ProdutoVendidoDTO> produtos) { this.produtos = produtos; }
}
