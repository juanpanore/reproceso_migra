package com.sura.arl.afiliados.modelo;

import com.sura.arl.reproceso.modelo.TipoPlanilla;

public class Legalizacion {
    private TipoPlanilla tipoPlanilla;
    private Long numeroFormulario;
    private TipoProceso tipoProceso;

    public TipoPlanilla getTipoPlanilla() {
        return tipoPlanilla;
    }

    public void setTipoPlanilla(TipoPlanilla tipoPlanilla) {
        this.tipoPlanilla = tipoPlanilla;
    }

    public Long getNumeroFormulario() {
        return numeroFormulario;
    }

    public void setNumeroFormulario(Long numeroFormulario) {
        this.numeroFormulario = numeroFormulario;
    }

    public TipoProceso getTipoProceso() {
        return tipoProceso;
    }

    public void setTipoProceso(TipoProceso tipoProceso) {
        this.tipoProceso = tipoProceso;
    }

	public enum TipoProceso {
		E("E"), I("I");

		private String equivalencia;
		
		TipoProceso(String equivalencia){
			this.setEquivalencia(equivalencia);
		}

		public String getEquivalencia() {
			return equivalencia;
		}

		public void setEquivalencia(String equivalencia) {
			this.equivalencia = equivalencia;
		}
		
		   public static TipoProceso tipoProcesoPorEquivalencia(String equivalencia) {
		        for (TipoProceso t : TipoProceso.values()) {
		            if (equivalencia.equals(t.getEquivalencia())) {
		                return t;
		            }
		        }
		        return null;
		    }

	}

    public Legalizacion cloneLegalizacion(){
        
        Legalizacion legalizacion = new Legalizacion();
        legalizacion.setNumeroFormulario(this.getNumeroFormulario());
        legalizacion.setTipoPlanilla(this.getTipoPlanilla());
        legalizacion.setTipoProceso(this.getTipoProceso());
        return legalizacion;
    }
}
