package com.sura.arl.estadocuenta.accesodatos;

import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.estadocuenta.modelo.ControlEstadoCuenta;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class ControlEstadoCuentaDao extends AbstractDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(ControlEstadoCuentaDao.class);
	
	@Transactional
	public void registrar(ControlEstadoCuenta control) {

		Map<String, Object> params = new HashMap<>();
		params.put("periodoGeneracion", control.getPeriodoGeneracion());
		params.put("periodoCotizacion", control.getPeriodoCotizacion());
		params.put("poliza", control.getPoliza());
		params.put("totalAfiliadosInicial", control.getTotalAfiliadosInicial());
		params.put("totalTrabajadoresInicial", control.getTotalTrabajadoresInicial());
		params.put("totalTrabajadores", control.getTotalTrabajadores());
		params.put("estadoPago", control.getEstadoPago().getEquivalencia());
		params.put("fechaLimitePago", control.getFechaLimitePago());
		params.put("tipoAfiliado", control.getTipoAfiliadoControl());
		params.put("usuarioOperacion", getVarEntorno().getValor("usuario.dniingresa"));
		params.put("conciliado", control.getConciliado().name());
		params.put("reabierto", control.getReabierto().name());
		params.put("saldoFavor", control.getSaldoFavor());
		params.put("deuda", control.getDeuda());
		params.put("expuestosInicial", control.getExpuestosInicial());
		params.put("valorEsperadoInicial", control.getValorEsperadoInicial());
		params.put("valorEsperado", control.getValorEsperado());
		params.put("valorSaldoInicial", control.getValorSaldoInicial());
		params.put("estadoPago", control.getEstadoPago().getEquivalencia());

		getJdbcTemplate().update(getVarEntorno().getValor("registro.controlestadocuenta"), params);
	}
	
	public ControlEstadoCuenta consultar(String periodoCotizacion, String poliza, String tipoAfiliadoControl) {

		Map<String, Object> params = new HashMap<>();
		params.put("periodoCotizacion", periodoCotizacion);
		params.put("poliza", poliza);
		params.put("tipoAfiliado", tipoAfiliadoControl);

		try {
			return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.controlestadocuenta"), params,
					(ResultSet rs, int index) -> {

						ControlEstadoCuenta.Builder builder = new ControlEstadoCuenta.Builder();
						builder.periodoCotizacion(periodoCotizacion);
						builder.totalAfiliadosInicial(rs.getInt("totalAfiliadosInicial"));
						builder.totalTrabajadoresInicial(rs.getInt("totalTrabajadoresInicial"));
						builder.fechaLimitePago(rs.getDate("fechaLimitePago"));
						builder.poliza(poliza);
						builder.tipoAfiliadoControl(rs.getString("tipoAfiliado"));

						return builder.build();

					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public Optional<String> consultarPeriodoCotizacion(String periodoGeneracion, String tipoAfiliado) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("periodoGeneracion", periodoGeneracion);
		params.put("tipoAfiliado", tipoAfiliado);

		try {
			return Optional.ofNullable(getJdbcTemplate().queryForObject(
					getVarEntorno().getValor("obtener.periodoCotizacion.tipoAfiliado"), params, String.class));
		} catch (DataAccessException dae) {
			return Optional.empty();
		}
	}

	public Optional<Date> consultaFechaLimitePago(String poliza, String periodo) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("poliza", poliza);
		params.put("periodo", periodo);

		try {
			return Optional.ofNullable(getJdbcTemplate().queryForObject(
					getVarEntorno().getValor("obtener.fechaLimitePago.polizaXPeriodo"), params, Date.class));
		} catch (DataAccessException dae) {
			return Optional.empty();
		}
	}
	
	@Transactional
	public void actualizar(String poliza, String periodoCotizacion, String tipoAfiliadoControl,
			long totalTrabajadoresInicial, long expuestosInicial, double valorEsperadoInicial) {

		Map<String, Object> params = new HashMap<>();
		params.put("poliza", poliza);
		params.put("periodoCotizacion", periodoCotizacion);
		params.put("tipoAfiliado", tipoAfiliadoControl);
		params.put("totalTrabajadoresInicial", totalTrabajadoresInicial);
		params.put("totalTrabajadores", totalTrabajadoresInicial);
		params.put("expuestosInicial", expuestosInicial);
		params.put("valorEsperadoInicial", valorEsperadoInicial);
		params.put("valorEsperado", valorEsperadoInicial);
		params.put("usuarioOperacion", getVarEntorno().getValor("usuario.dniingresa"));

		getJdbcTemplate().update(getVarEntorno().getValor("actualizacion.integrador.controlestadocuenta"), params);
	}

	@Transactional
	public void actualizarConsolidadoExcluyendoTipoCotizante(String poliza, String periodoCotizacion, String tipoAfiliado,
			String tipoCotizanteAExcluir, String tipoAfiliadoControl) {

		Map<String, Object> params = new HashMap<>();
		params.put("poliza", poliza);
		params.put("periodoCotizacion", periodoCotizacion);
		params.put("tipoAfiliado", tipoAfiliado);
		params.put("tipoAfiliadoControl", tipoAfiliadoControl);
		params.put("tipoCotizante", tipoCotizanteAExcluir);

		String sql = getVarEntorno().getValor("consolidar.controlestadocuenta.excluyendoTipoCotizante");

		try {
			
			getJdbcTemplate().update(sql, params);
		} catch(Exception e){
			
			LOG.error("Error al consolidar control poliza {}, periodoCot {}, tipoAfiliado {} , tipoAfControl {}, tipoCotizante exc {} ", 
					poliza, periodoCotizacion, tipoAfiliado, tipoAfiliadoControl, tipoCotizanteAExcluir,
					e);
		}
	}
	
	@Transactional
	public void actualizarConsolidadoTipoCotizanteOpcional(String poliza, String periodoCotizacion, String tipoAfiliado,
			String tipoCotizanteOpcional, String tipoAfiliadoControl) {

		Map<String, Object> params = new HashMap<>();
		params.put("poliza", poliza);
		params.put("periodoCotizacion", periodoCotizacion);
		params.put("tipoAfiliado", tipoAfiliado);
		params.put("tipoAfiliadoControl", tipoAfiliadoControl);
		params.put("tipoCotizante", tipoCotizanteOpcional);
		
		String sql = getVarEntorno().getValor("consolidar.controlestadocuenta.tipoCotizanteOpcional");
		
		try {
		
			getJdbcTemplate().update(sql, params);
		} catch(Exception e){
			
			LOG.error("Error al consolidar control poliza {}, periodoCot {}, tipoAfiliado {} , tipoAfControl {}, tipoCotizante {} ", 
					poliza, periodoCotizacion, tipoAfiliado, tipoAfiliadoControl, tipoCotizanteOpcional,
					e);
		}
	}

	
    @Transactional
    public void actualizarConsolidado(String periodo, String poliza, String fuente, String dni, boolean estudiantes,
            boolean independientes) {

        String sql;
        
        // actualmente la fuente no se usa,
        // se parametriza por si se necesita para registra la fuente de actualizacion
        Map<String, Object> params = new HashMap<>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        params.put("dni", dni);
        //params.put("fuente", fuente);
        
        if(estudiantes) {
            sql = "actualizacion.consolidado.estudiantes";
        }else if(independientes) {
            sql = "actualizacion.consolidado.independientes";
        }else {
            sql = "actualizacion.consolidado.dependientes";
        }
        getJdbcTemplate().update(getVarEntorno().getValor(sql), params);
    }
    
    public void actualizarTodosConsolidados(String periodo, String poliza, String fuente, String dni) {
        actualizarConsolidadoEstudiantes(periodo, poliza, fuente, dni);
        actualizarConsolidadoIndependientes(periodo, poliza, fuente, dni);
        actualizarConsolidadoDependientes(periodo, poliza, fuente, dni);
    }

    public void actualizarConsolidadoEstudiantes(String periodo, String poliza, String fuente, String dni) {
        actualizarConsolidado(periodo, poliza, fuente, dni, true, false);
    }

    public void actualizarConsolidadoIndependientes(String periodo, String poliza, String fuente, String dni) {
        actualizarConsolidado(periodo, poliza, fuente, dni, false, true);
    }

    public void actualizarConsolidadoDependientes(String periodo, String poliza, String fuente, String dni) {
        actualizarConsolidado(periodo, poliza, fuente, dni, false, false);
    }
    
    
    @Transactional
    public void actualizarRenes(String periodo, String poliza, String fuente, String dni) {
        
        // actualmente la fuente no se usa,
        // se parametriza por si se necesita para registra la fuente de actualizacion
        Map<String, Object> params = new HashMap<>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        //params.put("fuente", fuente);
        getJdbcTemplate().update(getVarEntorno().getValor("actualizacion.renes.consolidado.estudiantes"), params);
        getJdbcTemplate().update(getVarEntorno().getValor("actualizacion.renes.consolidado.dependientes"), params);
        getJdbcTemplate().update(getVarEntorno().getValor("actualizacion.renes.consolidado.independientes"), params);
    }
}
