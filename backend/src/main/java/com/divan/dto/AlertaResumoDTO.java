package com.divan.dto;

public class AlertaResumoDTO {
    private int conflitos;
    private int checkoutsVencidos;
    private int noShow;
    private int total;
          
    public void setConflitos(int conflitos) {
		this.conflitos = conflitos;
	}

	public void setCheckoutsVencidos(int checkoutsVencidos) {
		this.checkoutsVencidos = checkoutsVencidos;
	}

	public void setNoShow(int noShow) {
		this.noShow = noShow;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public AlertaResumoDTO(int conflitos, int checkoutsVencidos, int noShow) {
        this.conflitos = conflitos;
        this.checkoutsVencidos = checkoutsVencidos;
        this.noShow = noShow;
        this.total = conflitos + checkoutsVencidos + noShow;
    }

    public int getConflitos() { return conflitos; }
    public int getCheckoutsVencidos() { return checkoutsVencidos; }
    public int getNoShow() { return noShow; }
    public int getTotal() { return total; }
}