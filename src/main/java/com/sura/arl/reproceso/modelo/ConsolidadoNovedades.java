package com.sura.arl.reproceso.modelo;

import java.util.List;

public class ConsolidadoNovedades {
    private List<DatosNovedades> laboradas;
    private List<DatosNovedades> ausentismo;
    private List<DatosNovedades> laboradasOriginal;
    private List<DatosNovedades> ausentismoOriginal;
    
    private List<InfoNovedadVCT> listaVct;
    private String ley;
    private List<Long> formulariosAfectados;

    public List<DatosNovedades> getLaboradas() {
        return laboradas;
    }

    public void setLaboradas(List<DatosNovedades> laboradas) {
        this.laboradas = laboradas;
    }

    public List<DatosNovedades> getAusentismo() {
        return ausentismo;
    }

    public void setAusentismo(List<DatosNovedades> ausentismo) {
        this.ausentismo = ausentismo;
    }

    public List<InfoNovedadVCT> getListaVct() {
        return listaVct;
    }

    public void setListaVct(List<InfoNovedadVCT> listaVct) {
        this.listaVct = listaVct;
    }
    
    public List<DatosNovedades> getLaboradasOriginal() {
        return laboradasOriginal;
    }

    public void setLaboradasOriginal(List<DatosNovedades> laboradasOriginal) {
        this.laboradasOriginal = laboradasOriginal;
    }

    public List<DatosNovedades> getAusentismoOriginal() {
        return ausentismoOriginal;
    }

    public void setAusentismoOriginal(List<DatosNovedades> ausentismoOriginal) {
        this.ausentismoOriginal = ausentismoOriginal;
    }

    public String getLey() {
        return ley;
    }

    public void setLey(String ley) {
        this.ley = ley;
    }

    public List<Long> getFormulariosAfectados() {
        return formulariosAfectados;
    }

    public void setFormulariosAfectados(List<Long> formulariosAfectados) {
        this.formulariosAfectados = formulariosAfectados;
    }

}