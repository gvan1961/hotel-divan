package com.divan.dto;

import java.util.List;

public class LancamentoRapidoRequest {
    
    private List<ComandaRapidaDTO> comandas;
    
    public List<ComandaRapidaDTO> getComandas() {
        return comandas;
    }
    
    public void setComandas(List<ComandaRapidaDTO> comandas) {
        this.comandas = comandas;
    }
}