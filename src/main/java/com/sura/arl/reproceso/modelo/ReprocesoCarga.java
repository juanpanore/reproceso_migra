package com.sura.arl.reproceso.modelo;

public class ReprocesoCarga {

    private boolean masivo = false;
    private Long numeroFormulario;

    public boolean isMasivo() {
        return masivo;
    }

    public void setMasivo(boolean masivo) {
        this.masivo = masivo;
    }

    public Long getNumeroFormulario() {
        return numeroFormulario;
    }

    public void setNumeroFormulario(Long numeroFormulario) {
        this.numeroFormulario = numeroFormulario;
    }

}
