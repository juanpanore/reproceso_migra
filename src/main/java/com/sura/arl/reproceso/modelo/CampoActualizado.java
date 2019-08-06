package com.sura.arl.reproceso.modelo;

public class CampoActualizado {

    private String dni;
    private String nmperiodo;
    private Campo campo;
    private String dniModifica;
    private String valorViejo;
    private String valorNuevo;
    private String dniIngresa;
    private String poliza;
    private String tipoCotizante;
    private String tipoAfiliado;

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNmperiodo() {
        return nmperiodo;
    }

    public void setNmperiodo(String nmperiodo) {
        this.nmperiodo = nmperiodo;
    }

    public Campo getCampo() {
        return campo;
    }

    public void setCampo(Campo campo) {
        this.campo = campo;
    }

    public String getDniModifica() {
        return dniModifica;
    }

    public void setDniModifica(String dniModifica) {
        this.dniModifica = dniModifica;
    }

    public String getValorViejo() {
        return valorViejo;
    }

    public void setValorViejo(String valorViejo) {
        this.valorViejo = valorViejo;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getDniIngresa() {
        return dniIngresa;
    }

    public void setDniIngresa(String dniIngresa) {
        this.dniIngresa = dniIngresa;
    }

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getTipoCotizante() {
        return tipoCotizante;
    }

    public String getTipoAfiliado() {
        return tipoAfiliado;
    }

    public void setTipoAfiliado(String tipoAfiliado) {
        this.tipoAfiliado = tipoAfiliado;
    }

    public void setTipoCotizante(String tipoCotizante) {
        this.tipoCotizante = tipoCotizante;
    }

    public static Builder builder() {
        return new Builder();
    }

    public CampoActualizado(Builder builder) {
        super();
        this.dni = builder.dni;
        this.nmperiodo = builder.nmperiodo;
        this.campo = builder.campo;
        this.dniModifica = builder.dniModifica;
        this.valorViejo = builder.valorViejo;
        this.valorNuevo = builder.valorNuevo;
        this.dniIngresa = builder.dniIngresa;
        this.poliza = builder.poliza;
        this.tipoCotizante = builder.tipoCotizante;
        this.tipoAfiliado = builder.tipoAfiliado;
    }

    public static class Builder {
        private String dni;
        private String nmperiodo;
        private Campo campo;
        private String dniModifica;
        private String valorViejo;
        private String valorNuevo;
        private String poliza;
        private String dniIngresa;
        private String tipoCotizante;
        private String tipoAfiliado;

        public Builder dni(String valor) {
            this.dni = valor;
            return this;
        }

        public Builder nmperiodo(String valor) {
            this.nmperiodo = valor;
            return this;
        }

        public Builder campo(Campo valor) {
            this.campo = valor;
            return this;
        }

        public Builder dniModifica(String valor) {
            this.dniModifica = valor;
            return this;
        }

        public Builder valorViejo(String valor) {
            this.valorViejo = valor;
            return this;
        }

        public Builder valorNuevo(String valor) {
            this.valorNuevo = valor;
            return this;
        }

        public Builder dniIngresa(String valor) {
            this.dniIngresa = valor;
            return this;
        }

        public Builder poliza(String valor) {
            this.poliza = valor;
            return this;
        }

        public Builder tipoCotizante(String valor) {
            this.tipoCotizante = valor;
            return this;
        }

        public Builder tipoAfiliado(String valor) {
            this.tipoAfiliado = valor;
            return this;
        }

        public CampoActualizado build() {
            return new CampoActualizado(this);
        }

    }

    public enum Campo {
        NPOLIZA, TIPO_AFILIADO, DIAS_ESPERADOS, COTIZACION_ESPERADA, IBC_ESPERADO, SALARIO, CT,
        TASA_ESPERADA, EXISTE_PAGO, CTP;
    }

}
