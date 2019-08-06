package com.sura.arl.reproceso.servicios;

import java.time.YearMonth;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.estadocuenta.accesodatos.CondicionesTipoCotizanteDao;
import com.sura.arl.estadocuenta.accesodatos.IbcCotizacionDao;
import com.sura.arl.estadocuenta.modelo.CondicionesTipoCotizante;
import com.sura.arl.reproceso.accesodatos.UltimosValoresCotizadosDao;
import com.sura.arl.reproceso.accesodatos.UltimosValoresCotizadosDao.RespuestaUltimoPeriodoCotizado;
import com.sura.arl.reproceso.accesodatos.UltimosValoresCotizadosDao.RespuestaUltimosValoresCotizados;
import com.sura.arl.reproceso.util.RedondeosUtil;

@Service
public class UltimosValoresCotizadosServicio {

    private static final int UNO = 1;
    private static final int E30 = 30;
    private static final int E0 = 0;
    private static final long L0 = 0L;
    private static final Logger LOG = LoggerFactory.getLogger(UltimosValoresCotizadosServicio.class);
    private static final int CUATRO = 4;
    private static final int CERO = 0;
    private static final String LEY_2388 = "2388";
    private static final String ENERO = "01";
    private static final String JUNIO = "06";
    private static final String DICIEMBRE = "12";

    private final UltimosValoresCotizadosDao ultimosValoresCotizadosDao;

    private final IbcCotizacionDao ibcCotizacionDao;

    private final CondicionesTipoCotizanteDao condicionesTipoCotizanteDao;

    @Autowired
    public UltimosValoresCotizadosServicio(UltimosValoresCotizadosDao ultimosValoresCotizadosDao,
            IbcCotizacionDao ibcCotizacionDao, CondicionesTipoCotizanteDao condicionesTipoCotizanteDao) {
        this.ultimosValoresCotizadosDao = ultimosValoresCotizadosDao;
        this.ibcCotizacionDao = ibcCotizacionDao;
        this.condicionesTipoCotizanteDao = condicionesTipoCotizanteDao;
    }

    public void actualizar(Afiliado afiliado, String periodoCotizacion) {
        String dniAfiliado = afiliado.getDni().substring(1);
        String tipoDocumento = afiliado.getDni().substring(0,1);
        String tipoCotizante = afiliado.getTipoCotizante();
        TipoDocumento tipoDocEmpleador = afiliado.getTipoDocumentoEmpleador();
        String dniEmpleador = afiliado.getDniEmpleador();

        RespuestaUltimoPeriodoCotizado ultimoPeriodoCotizado = ultimosValoresCotizadosDao
                        .obtenerUltimoPeriodoCotizado(tipoDocEmpleador, dniEmpleador, dniAfiliado, tipoCotizante, tipoDocumento);
        
        if(ultimoPeriodoCotizado==null) {
            LOG.info(
                    "No se actualizaron los ultimos valores cotizados porque no se obtuvieron valores para tipoDocumentoEmpleador {}, nroDocumentoEmpleador {}, dniAfiliado {} y tipoCotizante {}",
                    tipoDocEmpleador, dniEmpleador, dniAfiliado, tipoCotizante);
            return;
        }
        
        YearMonth ultimoPeriodoCot = YearMonth.of(
                Integer.valueOf(ultimoPeriodoCotizado.getPeriodo().substring(CERO, CUATRO)),
                Integer.valueOf(ultimoPeriodoCotizado.getPeriodo().substring(CUATRO)));
        YearMonth periodoCot = YearMonth.of(Integer.valueOf(periodoCotizacion.substring(CERO, CUATRO)),
                Integer.valueOf(periodoCotizacion.substring(CUATRO)));

        if ((ultimoPeriodoCot.isBefore(periodoCot) || ultimoPeriodoCot.equals(periodoCot))
                && esMesActualizacion(periodoCotizacion.substring(CERO, CUATRO))) {

            RespuestaUltimosValoresCotizados ruvc = ultimosValoresCotizadosDao.obtenerUltimosValoresCotizados(
                    ultimoPeriodoCotizado.getLey(), dniAfiliado, tipoCotizante, tipoDocEmpleador, dniEmpleador,
                    ultimoPeriodoCotizado.getPeriodo(),tipoDocumento);
            if (ruvc != null) {
                aplicarActualizacion(ruvc, ultimoPeriodoCotizado, afiliado);
            } else {
                LOG.info(
                        "No se actualizaron los ultimos valores cotizados porque no se obtuvieron valores para tipoDocumentoEmpleador {}, nroDocumentoEmpleador {}, dniAfiliado {} y tipoCotizante {}",
                        tipoDocEmpleador, dniEmpleador, dniAfiliado, tipoCotizante);
            }
        }

    }

    private void aplicarActualizacion(RespuestaUltimosValoresCotizados ruvc, RespuestaUltimoPeriodoCotizado rupc,
            Afiliado afiliado) {

        String dniAfiliado = afiliado.getDni().substring(1);
        String tipoDocumento = afiliado.getDni().substring(0,1);
        String tipoCotizante = afiliado.getTipoCotizante();
        TipoDocumento tipoDocEmpleador = afiliado.getTipoDocumentoEmpleador();
        String dniEmpleador = afiliado.getDniEmpleador();
        String poliza = afiliado.getCobertura().getPoliza();

        Long ultimoIBC = L0;
        Long ultimoSalario = L0;

        Double salarioMinimoPeriodo = ibcCotizacionDao.consultarSalarioMinimoXperiodo(rupc.getPeriodo());
        CondicionesTipoCotizante ctc = condicionesTipoCotizanteDao.obtenerCantidadSalariosTopesIBC(tipoCotizante);

        Double minTopeSalarioMinimo = salarioMinimoPeriodo * ctc.getCantidadMinimaSalarios();
        Double maxTopeSalarioMinimo = salarioMinimoPeriodo * ctc.getCantidadMaximaSalarios();

        if (LEY_2388.equals(rupc.getLey())) {

            Optional<RespuestaUltimosValoresCotizados> ruvcNoLaborado = ultimosValoresCotizadosDao
                    .obtenerUltimosValoresCotizadosNoLaborado(rupc.getLey(), dniAfiliado.substring(UNO), tipoCotizante,
                            tipoDocEmpleador, dniEmpleador, rupc.getPeriodo(), tipoDocumento);

            if (ruvc.getDias() >= E30 && ruvcNoLaborado.isPresent() && ruvcNoLaborado.get().getDias() == E0) {
                ultimoIBC = ruvc.getIbc();
            } else {
                if (ruvc.getSalario() == L0 && ruvcNoLaborado.isPresent()) {
                    ultimoIBC = ruvcNoLaborado.get().getSalario();
                } else {
                    ultimoIBC = ruvc.getSalario();
                }

                ultimoSalario = ultimoIBC;
            }
        } else {
            if (ruvc.getIbc() > 0) {
                if (ruvc.getDias() == E0) {
                    ultimoIBC = ruvc.getIbc() * E30;
                } else {
                    ultimoIBC = (ruvc.getIbc() / ruvc.getDias()) * E30;
                }

                if (ultimoIBC < minTopeSalarioMinimo) {
                    ultimoIBC = minTopeSalarioMinimo.longValue();
                } else if (ultimoIBC > maxTopeSalarioMinimo) {
                    ultimoIBC = maxTopeSalarioMinimo.longValue();
                }
            }

            ultimoSalario = ruvc.getSalario();
        }

        ultimoIBC = RedondeosUtil.redondearIbc(ultimoIBC.doubleValue(), rupc.getPeriodo()).longValue();
        ultimosValoresCotizadosDao.actualizar(dniAfiliado, tipoCotizante, poliza, ultimoIBC, ultimoSalario,
                rupc.getPeriodo(), ruvc.getEps(), ruvc.getAfp());
    }

    private boolean esMesActualizacion(String mes) {
        // Es valido solo si no es ni Enero, ni Junio ni Diciembre
        if (!ENERO.equals(mes) && !JUNIO.equals(mes) && !DICIEMBRE.equals(mes)) {
            return true;
        }

        return false;
    }

}
