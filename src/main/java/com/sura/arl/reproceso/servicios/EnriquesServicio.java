package com.sura.arl.reproceso.servicios;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.estadocuenta.servicios.EstadoCuentaServicio;
import com.sura.arl.reproceso.accesodatos.EnriquesDao;
import com.sura.arl.reproceso.util.UtilFechas;
import java.util.HashMap;
import java.util.Map;

@Service
public class EnriquesServicio {

    private static final Logger LOG = LoggerFactory.getLogger(EnriquesServicio.class);

    private final EstadoCuentaServicio estadoCuentaServicio;

    private static final Integer TOPE_SALARIO_MINIMO = 25;
    
    private final EnriquesDao enriquesDao;

    @Autowired
    public EnriquesServicio(EstadoCuentaServicio estadoCuentaServicio,  EnriquesDao enriquesDao) {
        super();
        this.estadoCuentaServicio = estadoCuentaServicio;
        this.enriquesDao = enriquesDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void validarProcesoInconsistenciaNoPagado(Long numFormulario, Afiliado afiliado) {
        estadoCuentaServicio.marcarPagoDeAfiliado(numFormulario, afiliado);
    }

    // @Scheduled(cron = "0 09 15 * * ?", zone = "GMT-5:00")
    public void iniciarProcesoInconsistenciasNoPagados() {
        LOG.info("<<---------------------Proceso ENR Masivo--------------------->>");

        Date fechaProceso = UtilFechas.fechaActualSinHora();// UtilFechas.obtenerFechaSinHora("10",
                                                            // "7", "2017");

        List<Cobertura> coberturas = estadoCuentaServicio.consultarCoberturas(fechaProceso);
        LOG.info("+ Total empresas {}", coberturas.size());

        long t1 = System.currentTimeMillis();

        coberturas.stream().forEach(cobertura -> {
            estadoCuentaServicio.marcarPagoDeAfiliados(cobertura, fechaProceso);
            LOG.info("Enrique con exito en {} ms", (System.currentTimeMillis() - t1));
        });

        if (coberturas.size() > 0) {
            marcarIndependientesMas25SalariosMinimos(coberturas);
        }
    }

    private void marcarIndependientesMas25SalariosMinimos(List<Cobertura> coberturas) {

        String periodo = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")); // "201707";
        LOG.info("Periodo para buscar marcar independientes {} ", periodo);

        String cadenaPolizas = coberturas.stream().map(cobertura -> cobertura.getPoliza())
                .collect(Collectors.joining(","));
        LOG.info("cadenaPolizas {} ", cadenaPolizas);

        List<Afiliado> resultado = estadoCuentaServicio.obtenerSumatoriaIbcPorIndependiente(periodo, cadenaPolizas,
                TOPE_SALARIO_MINIMO);
        LOG.info("Numero de polizas de independientes {} ", resultado.size());

        resultado.stream().forEach(afiliado -> LOG.info("Afiliado cumple {}", afiliado.getDni()));

        LOG.info("Termine de procesar afiliado ibc mayor");
        estadoCuentaServicio.marcarPagoIndependiente(resultado);
    }
    
    public Integer marcarEnriques(Afiliado afiliado, String modifica){
        return enriquesDao.marcarEnrique(afiliado.getCobertura().getPoliza(),
                afiliado.getCobertura().getPeriodo(), 
                afiliado.getTipoCotizante(), 
                afiliado.getTipoAfiliado(),
                afiliado.getDni(), 
                modifica);
    }
    
    public Boolean existePago(Afiliado afiliado){
        return enriquesDao.existe(afiliado.getCobertura().getPoliza(), 
                afiliado.getDni(), 
                afiliado.getCobertura().getPeriodo(), 
                afiliado.getTipoAfiliado(), 
                afiliado.getTipoCotizante());
    }
   
}
