package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.Consolidado;
import com.sura.arl.reproceso.modelo.EstadoPagoControl;

@Repository
public class ConsolidadoEstadoCuentaDao extends AbstractDAO {

    public List<Consolidado> consultarConsolidados(String poliza, String periodo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("poliza", poliza);
        params.put("periodo", periodo);
        
        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.consolidados"), params,
                (ResultSet rs, int index) -> {
                    Consolidado consolidado = new Consolidado();
                    consolidado.setPoliza(poliza);
                    consolidado.setDeuda(rs.getDouble("PTDEUDA"));
                    consolidado.setOtrosConceptos(rs.getDouble("PTOTROS_CONCEPTOS"));
                    consolidado.setSaldoAfavor(rs.getDouble("PTSALDO_A_FAVOR"));
                    consolidado.setTotalRenes(rs.getDouble("PTTOTAL_RENES"));
                    consolidado.setValorAnulado(rs.getDouble("PTVALOR_ANULADO"));
                    consolidado.setValorEsperado(rs.getDouble("PTVALOR_ESPERADO"));
                    consolidado.setValorEsperadoInicial(rs.getDouble("PTVALOR_ESPERADOS_INICIAL"));
                    consolidado.setFechaLimitePago(rs.getDate("FELIMITE_PAGO"));
                    consolidado.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
                    consolidado.setPeriodo(periodo);
                    return consolidado;
                });

    }
    
    public List<Consolidado> consultarConsolidadosPorFechaLimite(Date fechaLimitePago) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fechaLimitePago", fechaLimitePago);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.consolidado.fechaLimite"), params,
                (ResultSet rs, int index) -> {
                    Consolidado consolidado = new Consolidado();
                    consolidado.setPoliza(rs.getString("NMPOLIZA"));
                    consolidado.setDeuda(rs.getDouble("PTDEUDA"));
                    consolidado.setOtrosConceptos(rs.getDouble("PTOTROS_CONCEPTOS"));
                    consolidado.setSaldoAfavor(rs.getDouble("PTSALDO_A_FAVOR"));
                    consolidado.setTotalRenes(rs.getDouble("PTTOTAL_RENES"));
                    consolidado.setValorAnulado(rs.getDouble("PTVALOR_ANULADO"));
                    consolidado.setValorEsperado(rs.getDouble("PTVALOR_ESPERADO"));
                    consolidado.setValorEsperadoInicial(rs.getDouble("PTVALOR_ESPERADOS_INICIAL"));
                    consolidado.setPeriodo(rs.getString("NMPERIODO"));
                    // consolidado.setPeriodoCotizacion(rs.getString("periodoCotizacion"));
                    return consolidado;
                });
    }

    @Transactional
    public void actualizarEstadoConsolidados(String periodo, String poliza, String fuente, String dni, EstadoPagoControl estado) {

        Map<String, Object> params = new HashMap<>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        params.put("dni", dni);
        params.put("fuente", fuente);
        params.put("estado", estado.getEquivalencia());
        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.estado.consolidados"), params);
    }

}
