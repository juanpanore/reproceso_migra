package com.sura.arl.reproceso.accesodatos;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.HistoricoInconsistenciaCambioCT;
import com.sura.arl.reproceso.modelo.excepciones.ReprocesoAfiliadoCanceladoExcepcion;

@Repository
public class HistoricoInconsistenciaCambioCTDao extends AbstractDAO {

    public void insertar(HistoricoInconsistenciaCambioCT registro) throws ReprocesoAfiliadoCanceladoExcepcion {

        Map<String, Object> params = new HashMap<>(7);
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("periodo", registro.getNmperiodo());
        params.put("sucursalAnterior", registro.getSucursalAnterior());
        params.put("sucursalNueva", registro.getSucursalNueva());
        params.put("codigo", registro.getCodigoInconsistencia().getEquivalencia());
        params.put("dniIngresa", registro.getDniIngresa());
        params.put("snCambio", registro.seCambia() ? "S" : "N");

        try {
            getJdbcTemplate().update(getVarEntorno().getValor("insertar.historicoInconsistenciaCT"), params);
        } catch (DataAccessException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                // intenta reenviar con unos segundos de retrazo
                try {
                    Thread.sleep(1000);
                    insertar(registro);
                } catch (InterruptedException e1) {
                    throw new ReprocesoAfiliadoCanceladoExcepcion(CatalogoErrores.ERROR_CAMBIO_CT);
                }
            }
        }
    }

    public void reportar(String dni, String periodo, String poliza, String ctOriginal, String ctReportado,
            InconsistenciaCambioCT inconsistencia, boolean seCambia) throws ReprocesoAfiliadoCanceladoExcepcion {
        HistoricoInconsistenciaCambioCT registro = new HistoricoInconsistenciaCambioCT();
        registro.setCodigoInconsistencia(inconsistencia);
        registro.setCambio(seCambia);
        registro.setDni(dni);
        registro.setNmperiodo(periodo);
        registro.setPoliza(poliza);
        registro.setSucursalAnterior(ctOriginal);
        registro.setSucursalAnterior(ctReportado);

        insertar(registro);

    }

    public enum InconsistenciaCambioCT {
        CT_SIN_COBERTURA_PERIODO("07"), AFIL_MULTIPLES_COBERTURAS_PERIODO("08");

        private String equivalencia;

        InconsistenciaCambioCT(String equivalencia) {
            this.equivalencia = equivalencia;
        }

        public String getEquivalencia() {
            return this.equivalencia;
        }
    }

}
