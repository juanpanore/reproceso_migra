package com.sura.arl.reproceso.modelo;

import java.util.List;

public class ResultadoRecalculo {
	private Double ibc = 0D;
	private Double dias = 0D;
	private Double cotizacion = 0D;

	private Double ibcReportado = 0D;
	private Double diasReportados = 0D;
	private Double cotizacionReportada = 0D;
	private Double tasaReportada = 0D;
	
	private boolean tienePago;
		
	private List<DetallePago> pagos;

	public Double getIbc() {
		return ibc;
	}

	public void setIbc(Double ibc) {
		this.ibc = ibc;
	}

	public Double getDias() {
		return dias;
	}

	public void setDias(Double dias) {
		this.dias = dias;
	}

	public Double getCotizacion() {
		return cotizacion;
	}

	public void setCotizacion(Double cotizacion) {
		this.cotizacion = cotizacion;
	}

	public Double getIbcReportado() {
		return ibcReportado;
	}

	public void setIbcReportado(Double ibcReportado) {
		this.ibcReportado = ibcReportado;
	}

	public Double getDiasReportados() {
		return diasReportados;
	}

	public void setDiasReportados(Double diasReportados) {
		this.diasReportados = diasReportados;
	}

	public Double getCotizacionReportada() {
		return cotizacionReportada;
	}

	public void setCotizacionReportada(Double cotizacionReportada) {
		this.cotizacionReportada = cotizacionReportada;
	}

	public Double getTasaReportada() {
		return tasaReportada;
	}

	public void setTasaReportada(Double tasaReportada) {
		this.tasaReportada = tasaReportada;
	}
	
    public List<DetallePago> getPagos() {
        return pagos;
    }

    public void setPagos(List<DetallePago> pagos) {
        this.pagos = pagos;
    }
        
    public boolean tienePago() {
        return tienePago;
    }

    public void setTienePago(boolean tienePago) {
        this.tienePago = tienePago;
    }

    @Override
    public String toString() {
        return "VariablesCotizacion [ibc=" + ibc + ", dias=" + dias + ", cotizacion=" + cotizacion + ", ibcReportado="
                + ibcReportado + ", diasReportados=" + diasReportados + ", cotizacionReportada=" + cotizacionReportada
                + ", tasaReportada=" + tasaReportada + "]";
    }

}