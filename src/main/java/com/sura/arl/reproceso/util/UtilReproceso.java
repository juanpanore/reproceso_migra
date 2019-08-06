package com.sura.arl.reproceso.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.reproceso.modelo.DatosNovedades;
import com.sura.arl.reproceso.modelo.DetallePago;
import com.sura.arl.reproceso.modelo.TipoPlanilla;

public final class UtilReproceso {

    // redondeo ibc segun ley
    public static Double calcularIbc(Double ibcActual, Integer dias, String periodo) {
        // TODO verificar x q se hizo esta operacion, descuadra el calculo
        // Double nuevoIbc = (Double.valueOf(ibcActual) * dias) / N30;
        return RedondeosUtil.redondearIbc(ibcActual, periodo);
    }

    // redondeo cotizacion segun ley
    public static Double calcularCotizacion(Double tasa, Double ibc, String periodo) {
        Double cotizacionEsperada = (tasa * ibc) / 100;
        return RedondeosUtil.redondearCotizacion(cotizacionEsperada, periodo);
    }

    public static List<DetallePago> convertirNovedadesADetalles(Afiliado af, List<DatosNovedades> laboradas,
            List<DatosNovedades> ausentismo, Long consecutivoEstadoCuenta) {
        //List<DatosNovedades> union =  new ArrayList<>();
        
        List<DetallePago> detalles = new ArrayList<>();

        for (DatosNovedades n : laboradas) {
            DetallePago d = new DetallePago(af.getCobertura().getPoliza(), af.getDni(), n.getFechaPago(),
                    n.getTipoPlanilla(), n.getPeriodo(), n.getNumeroFormulario(), n.getResponsable(),
                    n.getTipoCotizante(), n.getPlanilla(), n.getSubTipoCotizante());            
            d.setDiasLaborados(n.getDias());
            d.setIbcLaborado(n.getIbc());
            d.setSalarioLaborado(n.getSalario());
            d.setTasaLaborado(n.getTasa());
            d.setCotizacionLaborado(n.getCotizacion().doubleValue());
            d.setConsecutivoEstadoCuenta(consecutivoEstadoCuenta);
            d.setIdNovedad(n.getId());
            
            if (n.isIngreso()) {
                d.setTieneIngreso(true);
            }
            if (n.isRetiro()) {
                d.setTieneRetiro(true);
            }
            detalles.add(d);
        }
        
        for (DatosNovedades n : ausentismo) {
            DetallePago d = new DetallePago(af.getCobertura().getPoliza(), af.getDni(), n.getFechaPago(),
                    n.getTipoPlanilla(), n.getPeriodo(), n.getNumeroFormulario(), n.getResponsable(),
                    n.getTipoCotizante(), n.getPlanilla(), n.getSubTipoCotizante());            
            d.setDiasAusentismo(n.getDias());
            d.setIbcAusentismo(n.getIbc());
            d.setSalarioAusentismo(n.getSalario());
            d.setTasaAusentismo(n.getTasa());
            d.setCotizacionAusentismo(n.getCotizacion().doubleValue());
            d.setConsecutivoEstadoCuenta(consecutivoEstadoCuenta);
            d.setIdNovedad(n.getId());
            
            if (n.isIngreso()) {
                d.setTieneIngreso(true);
            }
            if (n.isRetiro()) {
                d.setTieneRetiro(true);
            }
            detalles.add(d);
        }
        return detalles;
    }
    
    /*public static void main(String[] argv) throws Exception {
        
        Afiliado af = new Afiliado();
        af.setCobertura(new Cobertura());
        af.getCobertura().setPoliza("094001965");
        af.setDni("C1020399172");
        
        List<DatosNovedades> laboradas = new ArrayList<>();
        DatosNovedades l1 = new DatosNovedades();
        l1.setConsecutivo(null);
        l1.setCotizacion(20700L);
        l1.setDias(13D);
        l1.setFechaPago(new Date());
        l1.setIbc(846800D);
        l1.setIngreso(false);
        l1.setNumeroFormulario(24132354L);
        l1.setPeriodo("022019");
        l1.setPlanilla("8489650822");
        l1.setResponsable("N890901672");
        l1.setRetiro(true);
        l1.setSalario(null);
        l1.setSubTipoCotizante("00");
        l1.setTasa(2.436);
        l1.setTipoCotizante("01");
        l1.setTipoPlanilla(TipoPlanilla.E);
        l1.setNumeroFormulario(24132354L);
        laboradas.add(l1);
        
        DatosNovedades l2 = new DatosNovedades();
        l2.setConsecutivo(null);
        l2.setCotizacion(15300L);
        l2.setDias(15D);
        l2.setFechaPago(new Date());
        l2.setIbc(626763D);
        l2.setIngreso(true);
        l2.setNumeroFormulario(24132354L);
        l2.setPeriodo("022019");
        l2.setPlanilla("8489650822");
        l2.setResponsable("N890901672");
        l2.setRetiro(false);
        l2.setSalario(null);
        l2.setSubTipoCotizante("00");
        l2.setTasa(0.522);
        l2.setTipoCotizante("01");
        l2.setTipoPlanilla(TipoPlanilla.E);
        l2.setNumeroFormulario(24132354L);

        laboradas.add(l2);
        
        List<DatosNovedades> ausentismo = new ArrayList<>();
        
        DatosNovedades l3 = new DatosNovedades();
        l3.setConsecutivo(null);
        l3.setCotizacion(20700L);
        l3.setDias(15D);
        l3.setFechaPago(new Date());
        l3.setIbc(846800D);
        l3.setIngreso(false);
        l3.setNumeroFormulario(24132354L);
        l3.setPeriodo("022019");
        l3.setPlanilla("8489650822");
        l3.setResponsable("N890901672");
        l3.setRetiro(true);
        l3.setSalario(null);
        l3.setSubTipoCotizante("00");
        l3.setTasa(2.436);
        l3.setTipoCotizante("01");
        l3.setTipoPlanilla(TipoPlanilla.E);
        l3.setNumeroFormulario(24132354L);
        ausentismo.add(l3);
        
        DatosNovedades l5 = new DatosNovedades();
        l5.setConsecutivo(null);
        l5.setCotizacion(20700L);
        l5.setDias(1D);
        l5.setFechaPago(new Date());
        l5.setIbc(846800D);
        l5.setIngreso(false);
        l5.setNumeroFormulario(24132354L);
        l5.setPeriodo("022019");
        l5.setPlanilla("8489650822");
        l5.setResponsable("N890901672");
        l5.setRetiro(true);
        l5.setSalario(null);
        l5.setSubTipoCotizante("00");
        l5.setTasa(2.436);
        l5.setTipoCotizante("01");
        l5.setTipoPlanilla(TipoPlanilla.E);
        l5.setNumeroFormulario(24132354L);
        ausentismo.add(l5);
        
        DatosNovedades l4 = new DatosNovedades();
        l4.setConsecutivo(null);
        l4.setCotizacion(20700L);
        l4.setDias(1D);
        l4.setFechaPago(new Date());
        l4.setIbc(846800D);
        l4.setIngreso(false);
        l4.setNumeroFormulario(24132354L);
        l4.setPeriodo("022019");
        l4.setPlanilla("8489650822");
        l4.setResponsable("N890901672");
        l4.setRetiro(true);
        l4.setSalario(null);
        l4.setSubTipoCotizante("00");
        l4.setTasa(2.436);
        l4.setTipoCotizante("01");
        l4.setTipoPlanilla(TipoPlanilla.E);
        l4.setNumeroFormulario(24132354L);
        ausentismo.add(l4);
        
        DatosNovedades l6 = new DatosNovedades();
        l6.setConsecutivo(null);
        l6.setCotizacion(20700L);
        l6.setDias(1D);
        l6.setFechaPago(new Date());
        l6.setIbc(846800D);
        l6.setIngreso(false);
        l6.setNumeroFormulario(24132354L);
        l6.setPeriodo("022019");
        l6.setPlanilla("8489650822");
        l6.setResponsable("N890901672");
        l6.setRetiro(true);
        l6.setSalario(null);
        l6.setSubTipoCotizante("00");
        l6.setTasa(2.436);
        l6.setTipoCotizante("01");
        l6.setTipoPlanilla(TipoPlanilla.E);
        l6.setNumeroFormulario(24132354L);
        ausentismo.add(l6);
        List<DetallePago> lista = convertirNovedadesADetalles(af, laboradas,
                ausentismo,0L);
        
        lista.forEach(r->{System.out.println(r);});
    }*/
}
