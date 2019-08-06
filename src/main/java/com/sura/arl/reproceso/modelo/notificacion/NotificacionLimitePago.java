package com.sura.arl.reproceso.modelo.notificacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class NotificacionLimitePago {

    private Long consecutivo;
    private String dni;
    private Optional<String> correoAfiliado;
    private String nombreAfiliado;
    private Date FechaLimitePago;
    private String strFechaLimitePago;
    private String strFechaNotificacion;
    private String periodo;
    private String strPeriodo;
    private EstadoNotificacionLimitePago estadoNotificacion;
    private String poliza;
    private Long nroAfiliados;
    private String usuarioOperacion;
    private String nombreArchivo;
    private Date fechaNotificacion;
    private List<Observacion> observaciones;
    
    public boolean tieneErrores(){
        
        return  observaciones != null && observaciones.size() > 0;
    }

    public Long getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Long consecutivo) {
        this.consecutivo = consecutivo;
    }

    public Date getFechaLimitePago() {
        return FechaLimitePago;
    }

    public void setFechaLimitePago(Date fechaLimitePago) {
        FechaLimitePago = fechaLimitePago;
    }

    public String getStrFechaNotificacion() {
        return strFechaNotificacion;
    }

    public void setStrFechaNotificacion(String strFechaNotificacion) {
        this.strFechaNotificacion = strFechaNotificacion;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public EstadoNotificacionLimitePago getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public void setEstadoNotificacion(EstadoNotificacionLimitePago estadoNotificacion) {
        this.estadoNotificacion = estadoNotificacion;
    }

    public String getPoliza() {
        return poliza;
    }

    public void setPoliza(String poliza) {
        this.poliza = poliza;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Optional<String> getCorreoAfiliado() {
        return correoAfiliado;
    }

    public void setCorreoAfiliado(Optional<String> correoAfiliado) {
        this.correoAfiliado = correoAfiliado;
    }

    public String getStrFechaLimitePago() {
        return strFechaLimitePago;
    }

    public void setStrFechaLimitePago(String strFechaLimitePago) {
        this.strFechaLimitePago = strFechaLimitePago;
    }

    public String getNombreAfiliado() {
        return nombreAfiliado;
    }

    public void setNombreAfiliado(String nombreAfiliado) {
        this.nombreAfiliado = nombreAfiliado;
    }

    public String getStrPeriodo() {
        return strPeriodo;
    }

    public void setStrPeriodo(String strPeriodo) {
        this.strPeriodo = strPeriodo;
    }

    public String getUsuarioOperacion() {
        return usuarioOperacion;
    }

    public void setUsuarioOperacion(String usuarioOperacion) {
        this.usuarioOperacion = usuarioOperacion;
    }

    public Long getNroAfiliados() {
        return nroAfiliados;
    }

    public void setNroAfiliados(Long nroAfiliados) {
        this.nroAfiliados = nroAfiliados;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Date getFechaNotificacion() {
        return fechaNotificacion;
    }

    public void setFechaNotificacion(Date fechaNotificacion) {
        this.fechaNotificacion = fechaNotificacion;
    }
    
    public List<Observacion> getObservaciones(){
        return observaciones;
    }
    
    public void crearObservacion(String codigo, String descripcion){
        
        if(this.observaciones == null){
            this.observaciones = new ArrayList<Observacion>();
        }
        this.observaciones.add(new Observacion(codigo,descripcion));
    }
    
    public class Observacion {
        
        public String codigo;
        public String descripcion;
        
        public Observacion(String codigo, String descripcion){
            this.codigo = codigo;
            this.descripcion = descripcion;
        }
        
        
    }
}
