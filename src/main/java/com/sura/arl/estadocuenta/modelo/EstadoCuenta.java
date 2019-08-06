package com.sura.arl.estadocuenta.modelo;

import com.sura.arl.afiliados.modelo.Afiliado;
import java.util.Date;

public class EstadoCuenta {

    private final Afiliado afiliado;
    private final Long cotizacion;
    private final Double tasa;
    private final Long ibc;
    private final Integer dias;
    private final String centroTrabajo;
    private final String centroTrabajoPagador;
    private final Long cotizacionReportada;
    private final Double tasaReportada;
    private final Long ibcReportado;
    private final Integer diasReportados;
    private final String observaciones;
    private final EstadoPago estadoPago;
    private final String existePago;
    private final Double saldo;
    private final int numeroCoberturas;
    private String usuarioOperacion;
    private Date fechaLimitePago;
    private Long consecutivo;

    private EstadoCuenta(Afiliado afiliado, Long cotizacion, Double tasa, Long ibc, Integer dias, String centroTrabajo,
            String centroTrabajoPagador, Long cotizacionReportada, Double tasaReportada, Long ibcReportado,
            Integer diasReportados, String observaciones, EstadoPago estadoPago, String existePago, Double saldo,
            int numeroCoberturas, Long consecutivo) {
        this.afiliado = afiliado;
        this.cotizacion = cotizacion;
        this.tasa = tasa;
        this.ibc = ibc;
        this.dias = dias;
        this.centroTrabajo = centroTrabajo;
        this.centroTrabajoPagador = centroTrabajoPagador;
        this.cotizacionReportada = cotizacionReportada;
        this.tasaReportada = tasaReportada;
        this.ibcReportado = ibcReportado;
        this.diasReportados = diasReportados;
        this.observaciones = observaciones;
        this.estadoPago = estadoPago;
        this.existePago = existePago;
        this.saldo = saldo;
        this.numeroCoberturas = numeroCoberturas;
        this.consecutivo = consecutivo;
    }

    public Afiliado getAfiliado() {
        return afiliado;
    }

    public Long getCotizacion() {
        return cotizacion;
    }

    public Long getIbc() {
        return ibc;
    }

    public Integer getDias() {
        return dias;
    }

    public Double getTasa() {
        return tasa;
    }

    public String getCentroTrabajo() {
        return centroTrabajo;
    }

    public String getCentroTrabajoPagador() {
        return centroTrabajoPagador;
    }

    public Long getCotizacionReportada() {
        return cotizacionReportada;
    }

    public Double getTasaReportada() {
        return tasaReportada;
    }

    public Long getIbcReportado() {
        return ibcReportado;
    }

    public Integer getDiasReportados() {
        return diasReportados;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public String getExistePago() {
        return existePago;
    }

    public Double getSaldo() {
        return saldo;
    }

    public int getNumeroCoberturas() {
        return numeroCoberturas;
    }

    public Long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Long consecutivo) {
        this.consecutivo = consecutivo;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Afiliado afiliado;
        private Long cotizacion;
        private Double tasa;
        private Long ibc;
        private Integer dias;
        private String centroTrabajo;
        private String centroTrabajoPagador;
        private Long cotizacionReportada;
        private Double tasaReportada;
        private Long ibcReportado;
        private Integer diasReportados;
        private String observaciones;
        private EstadoPago estadoPago;
        private String existePago;
        private Double saldo;
        private int numeroCoberturas = 1;
        private Long consecutivo;

        public Builder afiliado(Afiliado valor) {
            this.afiliado = valor;
            return this;
        }

        public Builder cotizacion(Long valor) {
            this.cotizacion = valor;
            return this;
        }

        public Builder tasa(Double valor) {
            this.tasa = valor;
            return this;
        }

        public Builder ibc(Long valor) {
            this.ibc = valor;
            return this;
        }

        public Builder dias(Integer valor) {
            this.dias = valor;
            return this;
        }

        public Builder centroTrabajo(String valor) {
            this.centroTrabajo = valor;
            return this;
        }

        public Builder centroTrabajoPagador(String valor) {
            this.centroTrabajoPagador = valor;
            return this;
        }

        public Builder observaciones(String valor) {
            this.observaciones = valor;
            return this;
        }

        public Builder cotizacionReportada(Long valor) {
            this.cotizacionReportada = valor;
            return this;
        }

        public Builder tasaReportada(Double valor) {
            this.tasaReportada = valor;
            return this;
        }

        public Builder ibcReportado(Long valor) {
            this.ibcReportado = valor;
            return this;
        }

        public Builder diasReportados(Integer valor) {
            this.diasReportados = valor;
            return this;
        }

        public Builder estadoPago(EstadoPago valor) {
            this.estadoPago = valor;
            return this;
        }

        public Builder existePago(String valor) {
            this.existePago = valor;
            return this;
        }

        public Integer getDias() {
            return dias;
        }

        public Double getTasa() {
            return tasa;
        }

        public Long getIbc() {
            return ibc;
        }

        public String getExistePago() {
            return existePago;
        }

        public Builder saldo(Double valor) {
            this.saldo = valor;
            return this;
        }

        public Builder numeroCoberturas(int valor) {
            this.numeroCoberturas = valor;
            return this;
        }
        
        public Builder consecutivo(Long valor) {
            this.consecutivo = valor;
            return this;
        }

        public EstadoCuenta build() {
            EstadoCuenta estadoCuenta = new EstadoCuenta(afiliado, cotizacion, tasa, ibc, dias, centroTrabajo, centroTrabajoPagador,
                    cotizacionReportada, tasaReportada, ibcReportado, diasReportados, observaciones, estadoPago,
                    existePago, saldo, numeroCoberturas, consecutivo);
            
            estadoCuenta.setUsuarioOperacion(afiliado.getUsuarioOperacion());
            return estadoCuenta;
        }

    }

    public String getUsuarioOperacion() {
        return usuarioOperacion;
    }

    public void setUsuarioOperacion(String usuarioOperacion) {
        this.usuarioOperacion = usuarioOperacion;
    }

    public Date getFechaLimitePago() {
        return fechaLimitePago;
    }

    public void setFechaLimitePago(Date fechaLimitePago) {
        this.fechaLimitePago = fechaLimitePago;
    }

    @Override
    public String toString() {
        return "EstadoCuenta [afiliado=" + afiliado + ", cotizacion=" + cotizacion + ", tasa=" + tasa + ", ibc=" + ibc
                + ", dias=" + dias + ", centroTrabajo=" + centroTrabajo + ", centroTrabajoPagador="
                + centroTrabajoPagador + ", cotizacionReportada=" + cotizacionReportada + ", tasaReportada="
                + tasaReportada + ", ibcReportado=" + ibcReportado + ", diasReportados=" + diasReportados
                + ", observaciones=" + observaciones + ", estadoPago=" + estadoPago + ", existePago=" + existePago
                + ", saldo=" + saldo + ", numeroCoberturas=" + numeroCoberturas + "]";
    }

}
