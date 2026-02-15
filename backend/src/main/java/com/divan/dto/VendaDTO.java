package com.divan.dto;

import com.divan.entity.NotaVenda;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public class VendaDTO {
    
    private Long id;
    private LocalDateTime dataHoraVenda;
    private NotaVenda.TipoVendaEnum tipoVenda;
    private Long reservaId;
    private String apartamentoNumero;
    private BigDecimal total;
    private List<ItemVendaDTO> itens;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public LocalDateTime getDataHoraVenda() {
		return dataHoraVenda;
	}
	public void setDataHoraVenda(LocalDateTime dataHoraVenda) {
		this.dataHoraVenda = dataHoraVenda;
	}
	public NotaVenda.TipoVendaEnum getTipoVenda() {
		return tipoVenda;
	}
	public void setTipoVenda(NotaVenda.TipoVendaEnum tipoVenda) {
		this.tipoVenda = tipoVenda;
	}
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
	public String getApartamentoNumero() {
		return apartamentoNumero;
	}
	public void setApartamentoNumero(String apartamentoNumero) {
		this.apartamentoNumero = apartamentoNumero;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	public List<ItemVendaDTO> getItens() {
		return itens;
	}
	public void setItens(List<ItemVendaDTO> itens) {
		this.itens = itens;
	}    
    
}
