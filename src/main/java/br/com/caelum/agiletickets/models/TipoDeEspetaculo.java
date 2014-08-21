package br.com.caelum.agiletickets.models;

import java.math.BigDecimal;

public enum TipoDeEspetaculo {
	
	CINEMA(new BigDecimal(0.05), new BigDecimal(0.10)), 
	SHOW(new BigDecimal(0.05), new BigDecimal(0.10)), 
	TEATRO(new BigDecimal(0.05), new BigDecimal(0.10)), 
	BALLET(new BigDecimal(0.50), new BigDecimal(0.20)), 
	ORQUESTRA(new BigDecimal(0.50), new BigDecimal(0.20));
	
	public BigDecimal percentual;
	public BigDecimal multiplicador;
	
	private TipoDeEspetaculo(BigDecimal percentual, BigDecimal multiplicador){
		this.percentual = percentual;
		this.multiplicador = multiplicador;
	}
	
}
