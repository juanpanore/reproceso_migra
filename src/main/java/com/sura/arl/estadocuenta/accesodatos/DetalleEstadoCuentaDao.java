package com.sura.arl.estadocuenta.accesodatos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.DetallePago;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;

@Repository
public class DetalleEstadoCuentaDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(DetalleEstadoCuentaDao.class);

    @Transactional
    @SuppressWarnings("unchecked")
    public int registrarConConsecutivo(String poliza, Long consecutivo, Long consecutivoPagado) {

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("consecutivo", consecutivo);
            params.put("poliza", poliza);
            params.put("consecutivoPagado", consecutivoPagado);

            return getJdbcTemplate().update(getVarEntorno().getValor("registro.detalles.est.cta.consecutivo"), params);

        } catch (DataAccessException e) {
            LOG.error(
                    "Error al insertar Detalle Estado de Cuenta, consecutivo:{}, poliza:{}, consecutivoPagado:{} ->   ",
                    consecutivo, poliza, consecutivoPagado, e);
            throw new AccesoDatosExcepcion("Error al insertar Detalle");
        }
    }

    @Transactional
    public void registrar(List<DetallePago> detalles) {
        if (detalles.isEmpty()) {
            return;
        }

        int i = 0;
        Map<String, Object> params[] = new Map[detalles.size()];
        for (DetallePago detalle : detalles) {
            Map<String, Object> paramsDetalles = obtenerParametros(detalle);
            params[i++] = paramsDetalles;
        }

        int[] x = getJdbcTemplate().batchUpdate(getVarEntorno().getValor("registro.detalles.est.cta"), params);

    }

    private Map<String, Object> obtenerParametros(DetallePago detalle) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", detalle.getNpoliza());
        params.put("dniAfiliado", detalle.getDni());
        params.put("periodo", detalle.getPeriodo().substring(2).concat(detalle.getPeriodo().substring(0, 2)));
        params.put("numeroformulario", detalle.getNumeroFormulario());
        params.put("tipoCotizante", detalle.getTipoCotizante());
        params.put("subtipoCotizante", detalle.getSubTipoCotizante());
        params.put("tipoPlanilla", detalle.getTipoPlanilla().name());
        params.put("planilla", detalle.getPlanilla());
        params.put("fepago", detalle.getFechaPago());
        params.put("ingreso", detalle.tieneIngreso() ? "S" : "N");
        params.put("retiro", detalle.tieneRetiro() ? "S" : "N");
        params.put("responsable", detalle.getResponsable());
        params.put("diasAusentismo", detalle.getDiasAusentismo().intValue());
        params.put("tasaAusentismo", detalle.getTasaAusentismo());
        params.put("ibcAusentismo", detalle.getIbcAusentismo().intValue());
        params.put("cotizacionAusentismo", detalle.getCotizacionAusentismo().intValue());
        params.put("diasLaborados", detalle.getDiasLaborados().intValue());
        params.put("tasaLaborado", detalle.getTasaLaborado());
        params.put("ibcLaborado", detalle.getIbcLaborado().intValue());
        params.put("cotizacionLaborado", detalle.getCotizacionLaborado().intValue());
        params.put("dniIngresa", getVarEntorno().getValor("usuario.dniingresa"));
        params.put("consecutivoEstCta", detalle.getConsecutivoEstadoCuenta());
        params.put("idNovedad", detalle.getIdNovedad());
        return params;
    }

}
