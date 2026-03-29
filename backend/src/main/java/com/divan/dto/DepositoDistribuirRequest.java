package com.divan.dto;

public class DepositoDistribuirRequest {
    private Long itemId;
    private Long reservaId;
    private Integer quantidade;
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public Long getReservaId() {
		return reservaId;
	}
	public void setReservaId(Long reservaId) {
		this.reservaId = reservaId;
	}
	public Integer getQuantidade() {
		return quantidade;
	}
	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}
    
    
}
