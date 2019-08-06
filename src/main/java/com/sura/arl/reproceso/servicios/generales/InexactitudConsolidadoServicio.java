package com.sura.arl.reproceso.servicios.generales;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.estadocuenta.accesodatos.EstadoCuentaDao;
import com.sura.arl.reproceso.accesodatos.ConsolidadoEstadoCuentaDao;
import com.sura.arl.reproceso.accesodatos.HistoricoMovimientoEstadoCuentaDao;
import com.sura.arl.reproceso.accesodatos.LegalizacionDao;
import com.sura.arl.reproceso.accesodatos.ParametrosDao;
import com.sura.arl.reproceso.modelo.Consolidado;
import com.sura.arl.reproceso.modelo.EstadoPagoControl;
import com.sura.arl.reproceso.util.VariablesEntorno;

@Service
public class InexactitudConsolidadoServicio {

    private LegalizacionDao legalizacionDao;
    private Map<String, String> parametrosProceso;
    private final ConsolidadoEstadoCuentaDao consolidadoDao;
    private final HistoricoMovimientoEstadoCuentaDao hcoMvtoDao;
    private final EstadoCuentaDao estadoCtaDao;
    private static final Logger LOG = LoggerFactory.getLogger(InexactitudConsolidadoServicio.class);

    @Autowired
    private VariablesEntorno varEntorno;

    @Autowired
    public InexactitudConsolidadoServicio(LegalizacionDao legalizacionDao, ParametrosDao parametrosDao,
            ConsolidadoEstadoCuentaDao consolidadoDao, HistoricoMovimientoEstadoCuentaDao hcoMvtoDao,
            EstadoCuentaDao estadoCtaDao) {
        this.legalizacionDao = legalizacionDao;
        this.consolidadoDao = consolidadoDao;
        this.hcoMvtoDao = hcoMvtoDao;
        this.estadoCtaDao = estadoCtaDao;
        parametrosProceso = parametrosDao.obtenerTodosParametros();
    }

    public void procesarConsolidados(String poliza, String periodo) {
        List<Consolidado> consolidados = consolidadoDao.consultarConsolidados(poliza, periodo);
        Date fechaLimitePago = consolidados.get(0).getFechaLimitePago();
        LOG.debug("Verificando fecha limite de pago para poliza:{},periodo:{}--->{}", poliza, periodo,
                fechaLimitePago.toString());

        // verifica si la fechaLimPago ya paso
        if (fechaLimitePago.compareTo(new Date()) < 0) {
            LOG.debug("Fecha limite de pago {}, poliza:{}, esta vencida, se actualiza estado de consolidado y enrqs",
                    fechaLimitePago.toString(), poliza);
            consolidados.forEach(r -> procesarConsolidado(r));
            estadoCtaDao.actualizarNoPagosAenriques(poliza, periodo);
        }

    }

    public void procesarConsolidado(Consolidado consolidado) {
        Double saldoEstadoCuenta = consolidado.getSaldoAfavor() - consolidado.getDeuda()
                + consolidado.getOtrosConceptos() + consolidado.getTotalRenes() - consolidado.getValorAnulado();

        if (saldoEstadoCuenta == 0) {
            actualizarConsolidado(consolidado, EstadoPagoControl.AL_DIA);
        } else {
            Double totalPagado = legalizacionDao.obtenerTotalPagado(consolidado.getPoliza(), consolidado.getPeriodo());
            LOG.debug("total pagado -> poliza:{}, periodo:{} :: {}", consolidado.getPoliza(), consolidado.getPeriodo(),
                    totalPagado);

            if (totalPagado == 0) {
                actualizarConsolidado(consolidado, EstadoPagoControl.MORA_TOTAL);
            } else {

                Double porcentajeDiferencia = consolidado.getValorEsperadoInicial() / totalPagado * 100;
                if (porcentajeDiferencia <= Double
                        .valueOf(parametrosProceso.get("notificador.porcentaje.mora.parcial"))) {
                    actualizarConsolidado(consolidado, EstadoPagoControl.MORA_PARCIAL);
                } else {
                    actualizarConsolidado(consolidado, EstadoPagoControl.INEXACTITUD_PAGOS);
                }
            }
        }
    }

    private void actualizarConsolidado(Consolidado consolidado, EstadoPagoControl estado) {
        String dni = getVarEntorno().getValor("usuario.dniingresa");
        String fuente = getVarEntorno().getValor("fuente.proceso");

        consolidadoDao.actualizarEstadoConsolidados(consolidado.getPeriodo(), consolidado.getPoliza(), fuente, dni,
                estado);
    }

    public VariablesEntorno getVarEntorno() {
        return varEntorno;
    }

    public void setVarEntorno(VariablesEntorno varEntorno) {
        this.varEntorno = varEntorno;
    }

}
