package com.sura.arl.reproceso.accesodatos;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Legalizacion;
import com.sura.arl.afiliados.modelo.Legalizacion.TipoProceso;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.TipoPlanilla;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;

@Repository
public class ControlNovedadesDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ControlNovedadesDao.class);

    static final String S = "S";
    static final String N = "N";

    @Autowired
    public ControlNovedadesDao(PlatformTransactionManager transactionManager) {
    }

    @Transactional
    public void actualizarProcesado(Long numeroFormulario, boolean procesado) {

        if (numeroFormulario == null) {
            throw new AccesoDatosExcepcion("No es posible registrar sin datos");
        }
        
        ResultadoReprocesoNovedades resultado =  obtenerResultadoReproceso(numeroFormulario);

        Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("numeroFormulario", numeroFormulario);
        params.put("estado", procesado ? S : N);
        params.put("registrosBuenos", resultado.getTotalBuenas());
        params.put("registrosMalos", resultado.getTotalMalas());
        params.put("totalIbc", resultado.getTotalIbc());

        getJdbcTemplate().update(getVarEntorno().getValor("actualizar.estadoCarga.controlNovedades"), params);
    }

    @Deprecated
    public List<Long> getNumerosFormulario(String poliza, String afiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("tipo_documento", afiliado.substring(0, 1));
        params.put("documento", afiliado.substring(1));

        return getJdbcTemplate().query(getVarEntorno().getValor("buscar.numero.formulario.afiliado"), params,
                (ResultSet rs, int index) -> {
                    return rs.getLong("NMFORMULARIO_PAGO");
                });
    }
    
    public List<Long> getNumerosFormulario(String poliza, String afiliado, String periodo, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("tipo_documento", afiliado.substring(0, 1));
        params.put("documento", afiliado.substring(1));
        params.put("periodo", periodo);
        params.put("tipoAfiliado", tipoAfiliado);

        return getJdbcTemplate().query(getVarEntorno().getValor("buscar.numero.formulario.afiliado.periodo"), params,
                (ResultSet rs, int index) -> {
                    return rs.getLong("NMFORMULARIO_PAGO");
                });
    }
    
	public List<Legalizacion> consultarOtrosPagos(Afiliado afiliado) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tipo_documento", afiliado.getDni().substring(0, 1));
		params.put("documento", Integer.parseInt(afiliado.getDni().substring(1)));
		params.put("periodo", afiliado.getPeriodoCotizacion());

		try {
			return getJdbcTemplate().query(getVarEntorno().getValor("consulta.pagos.otros"), params,
					(ResultSet rs, int index) -> {

						Legalizacion pago = new Legalizacion();
						pago.setTipoPlanilla(
								TipoPlanilla.tipoPlanillaPorEquivalencia((rs.getString("CDTIPO_PLANILLA"))));
						pago.setTipoProceso(TipoProceso.tipoProcesoPorEquivalencia(rs.getString("CDTIPO_PROCESO")));
						
						return pago;

					});

		} catch (DataAccessException e) {
			LOG.error("Error al consultar otro pagos", e);

			return null;
		}

	}
    
    public ResultadoReprocesoNovedades obtenerResultadoReproceso(Long numeroFormulario) {

        String sql = getVarEntorno().getValor("consultar.resultado.reproceso");
    	
    	Map<String, Object> params = new HashMap<>();
        params.put("numeroFormulario", numeroFormulario);
        
        try {
            return getJdbcTemplate().queryForObject(sql, params, (ResultSet rs, int index) -> {
            	ResultadoReprocesoNovedades res = new ResultadoReprocesoNovedades();
                
                res.setTotalNovedades(rs.getLong("TOTAL"));
                res.setTotalBuenas(rs.getLong("BUENOS"));
                res.setTotalMalas(rs.getLong("MALOS"));
                res.setTotalIbc(rs.getBigDecimal("TOTALIBC"));
                return res;
            });
        } catch (EmptyResultDataAccessException erdac) {
            return null;
        }
    }
    
    private class ResultadoReprocesoNovedades{
    	private Long totalNovedades;
    	private Long totalBuenas;
    	private Long totalMalas;
    	private BigDecimal totalIbc;
		
    	public Long getTotalNovedades() {
			return totalNovedades;
		}
		public void setTotalNovedades(Long totalNovedades) {
			this.totalNovedades = totalNovedades;
		}
		public Long getTotalBuenas() {
			return totalBuenas;
		}
		public void setTotalBuenas(Long totalBuenas) {
			this.totalBuenas = totalBuenas;
		}
		public Long getTotalMalas() {
			return totalMalas;
		}
		public void setTotalMalas(Long totalMalas) {
			this.totalMalas = totalMalas;
		}
		public BigDecimal getTotalIbc() {
			return totalIbc;
		}
		public void setTotalIbc(BigDecimal totalIbc) {
			this.totalIbc = totalIbc;
		}
    	
    	
    }
}
