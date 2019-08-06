package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.HistoricoAfiliado;

@Repository
public class HistoricoAfiliadosDao extends AbstractDAO {

    public HistoricoAfiliado consultarRegistroAfectado(String poliza, String dni, Long certificado, String periodo) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("dni", dni);
        params.put("certificado", certificado);
        params.put("periodo", periodo);

        HistoricoAfiliado ha = new HistoricoAfiliado();
        ha.setDni(dni);
        ha.setCertificado(certificado);
        ha.setPoliza(poliza);

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.historicoAfiliado.afectado"),
                    params, (ResultSet rs, int index) -> {
                        ha.setFealta(rs.getDate("FEALTA"));
                        ha.setFebaja(rs.getDate("FEBAJA"));
                        ha.setFuente(rs.getString("CDFUENTE"));
                        ha.setPoliza(rs.getString("NPOLIZA"));
                        ha.setSucursal(rs.getString("CDSUCURSAL"));
                        ha.setSucursalPagadora(rs.getString("CDSUCURSAL_PAGADORA"));
                        ha.setTema(rs.getString("CDTEMA"));

                        return ha;
                    });
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void actualizarFebaja(HistoricoAfiliado registro, Date febajaOriginal) {

        Map<String, Object> params = new HashMap<>(5);
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("certificado", registro.getCertificado());
        params.put("febaja", registro.getFebaja());
        params.put("dniModifica", registro.getDniModifica());
        params.put("febajaOriginal", febajaOriginal);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.febaja.historicoAfiliado.afectado"), params);
    }
    
    public void actualizar(HistoricoAfiliado registro, Date febajaOriginal) {

        Map<String, Object> params = new HashMap<>(5);
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("certificado", registro.getCertificado());
        params.put("febaja", registro.getFebaja());
        params.put("dniModifica", registro.getDniModifica());
        params.put("dniIngresa", registro.getDniIngresa());
        params.put("sucursal", registro.getSucursal());
        params.put("sucursalPagadora", registro.getSucursalPagadora());
        params.put("dniModifica", registro.getDniModifica());
        params.put("febajaOriginal", febajaOriginal);

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.historicoAfiliado.afectado"), params);
    }

    public void insertar(HistoricoAfiliado registro) {

        Map<String, Object> params = new HashMap<>(6);
        params.put("poliza", registro.getPoliza());
        params.put("dni", registro.getDni());
        params.put("sucursal", registro.getSucursal());
        params.put("fealta", registro.getFealta());
        params.put("febaja", registro.getFebaja());
        params.put("certificado", registro.getCertificado());
        params.put("fuente", registro.getFuente());
        params.put("dniIngresa", registro.getDniIngresa());
        params.put("sucursalPagadora", registro.getSucursalPagadora());
        params.put("tema", registro.getTema());

        getJdbcTemplate().update(getVarEntorno().getValor("insertar.historicoAfiliado.afectado"), params);
    }

}
