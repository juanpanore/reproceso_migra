package com.sura.arl.reproceso.accesodatos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;
import com.sura.arl.afiliados.modelo.Legalizacion;
import com.sura.arl.afiliados.modelo.Legalizacion.TipoProceso;
import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.estadocuenta.modelo.CatalogoErrores;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.accesodatos.CoberturaDao.DatosCobertura;
import com.sura.arl.reproceso.actores.msg.ReprocesoAfiliadosMsg;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.TipoPlanilla;

@Repository
public class LogEjecucionReprocesoDao extends AbstractDAO {

	private static final Logger LOG = LoggerFactory.getLogger(LogEjecucionReprocesoDao.class);

	static final int NUMERO_REGISTROS_TRANSACCION = 2000;
	static final String S = "S";
	static final String N = "N";
	static final String X = "X";
	static final int N1 = 1;

	static final String[] tiposCotizantesEstudiantes = { "23" };
	static final TipoPlanilla[] tiposPlanillasEstudiantes = { TipoPlanilla.N, TipoPlanilla.K, TipoPlanilla.U,
			TipoPlanilla.M, TipoPlanilla.A };
	static final String TIPO_AFILIADO_INDEPENDIENTE = "02";
	static final String TIPO_AFILIADO_EMPRESA = "01";
	static final String TIPO_AFILIADO_ESTUDIANTE = "03";

	private final CoberturaDao coberturaDao;

	@Autowired
	public LogEjecucionReprocesoDao(CoberturaDao coberturaDao) {
		this.coberturaDao = coberturaDao;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void registrar(List<Afiliado> afiliados, TipoErrorEjecucion tipoError, String descripcionError) {

		try {
			if (afiliados.isEmpty()) {
				return;
			}

			int i = 0;
			Map<String, Object> params[] = new Map[afiliados.size()];
			for (Afiliado afiliado : afiliados) {
				Map<String, Object> paramsRegisatros = obtenerParametros(afiliado, tipoError, descripcionError);

				params[i++] = paramsRegisatros;
			}

			getJdbcTemplate().batchUpdate(getVarEntorno().getValor("registrar.logReproceso.ejecucion"), params);
		} catch (Exception e) {
			LOG.error("Se ha presentado un error al registrar errores en el log de ejecucion de reproceso ", e);
		}
	}

	@Transactional
	public void registrar(Afiliado afiliado, TipoErrorEjecucion tipoError, String descripcionError) {

		try {
			Map<String, Object> params = obtenerParametros(afiliado, tipoError, descripcionError);
			getJdbcTemplate().update(getVarEntorno().getValor("registrar.logReproceso.ejecucion"), params);
		} catch (Exception e) {
			LOG.error("Se ha presentado un error al registrar un error en el log de ejecucion de reproceso", e);
		}
	}

	@Transactional
	public void actualizar(long nmconsecutivo) {

		try {
			Map<String, Object> params = new HashMap<>();
			params.put("consecutivoRegistro", nmconsecutivo);
			params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));

			getJdbcTemplate().update(getVarEntorno().getValor("actualizar.logReproceso.ejecucion"), params);
		} catch (Exception e) {
			LOG.error(
					"Se ha presentado un error al actualizar el febaja en el log de ejecucion de reproceso para el consecutivo {}",
					nmconsecutivo, e);
		}
	}

	public List<ReprocesoAfiliadosMsg> obtenerAfiliadosParaReprocesar() {

		Map<String, Object> params = new HashMap<>();

		return getJdbcTemplate().query(getVarEntorno().getValor("consultar.logReproceso.ejecucion"), params,
				(rs, nm) -> {
					Afiliado af = new Afiliado();
					Cobertura cb = new Cobertura();
					Condicion cn = new Condicion();
					Legalizacion lg = new Legalizacion();
					InfoNovedadVCT iv = new InfoNovedadVCT();

					long nmconsecutivo = rs.getLong("NMCONSECUTIVO");

					af.setDni(rs.getString("DNI_AFILIADO"));
					af.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
					af.setTipoCotizante(rs.getString("CDTIPO_COTIZANTE"));
					af.setSubtipoCotizante(rs.getString("CDSUBTIPO_COTIZANTE"));

					af.setTipoDocumentoEmpleador(
							TipoDocumento.tipoDocumentoPorEquivalencia(rs.getString("CDTIPO_DOC_EMPLEADOR")));
					af.setDniEmpleador(rs.getString("NMDOCUMENTO_EMPLEADOR"));
					af.setPeriodoCotizacion(rs.getString("NMPERIODO"));

					// datos basicos de la cobertura
					cb.setPoliza(rs.getString("NMPOLIZA"));
					cb.setPeriodo(af.getPeriodoCotizacion());

					// datos de las condiciones
					// si la consulta no encontro relacion entre el tipoCot y
					// las condiciones
					// estos valores llegan null
					cn.setIndicadorDias(rs.getString("SNINDICADOR_PROPORCIONAL_DIAS"));
					cn.setTipoGeneracion(rs.getString("CDTIPO_GENERACION"));
					cn.setTipoNovedad(rs.getString("CDTIPO_NOVEDAD"));
					cn.setTipoTasa(rs.getString("CDTIPO_TASA"));
					cn.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
					cn.setIbcMaximo(rs.getInt("PTINGRESO_MAX_BASE_LIQ"));
					cn.setIbcMinimo(rs.getInt("PTINGRESO_MIN_BASE_LIQ"));

					// datos de la legalizacion
					lg.setNumeroFormulario(Long.valueOf(rs.getString("NMFORMULARIO")));
					lg.setTipoPlanilla(TipoPlanilla.valueOf(rs.getString("CDTIPO_PLANILLA")));
					lg.setTipoProceso(TipoProceso.valueOf(rs.getString("CDTIPO_PROCESO")));

					// datos de vct reportado
					iv.setCentroTrabajo(rs.getString("CDNUEVA_SUCURSAL"));
					iv.setFechaFinVCT(rs.getDate("FEINICIO_VCT"));
					iv.setFechaInicioVCT(rs.getDate("FEFIN_VCT"));
					iv.setSnvct(rs.getString("CDVALOR_VCT"));

					// setea los objs del afiliado
					af.setCobertura(cb);
					af.setCondicion(cn);
					af.setLegalizacion(lg);
					af.setInfoVct(iv);

					if (TipoProceso.I.equals(af.getLegalizacion().getTipoProceso())) {
						af.setEsIndependiente(true);
					}

					// en caso de las variables de las condiciones esten null,
					// implica que no encontro homologado el tipoCond en
					// TCPG_CONDICIONES_COTIZANTE
					// se reporta error
					if (cn.getTipoGeneracion() == null) {
						af.setTipoError(CatalogoErrores.COBERTURA_INVALIDA);
						return ReprocesoAfiliadosMsg.crear(nmconsecutivo, af);
					}

					// se buscan todas las coberturas
					List<DatosCobertura> datosCobertura = coberturaDao.consultarDatosCoberturaAfiliados(af.getDni(),
							cb.getPoliza(), cb.getPeriodo());

					af.getCobertura().setTotalCoberturas(datosCobertura.size());

					// setea si es independiente o no, segun la informacion ya
					// validada desde la
					// legalizacion
					if (Arrays.stream(tiposCotizantesEstudiantes).anyMatch(af.getTipoCotizante()::equals)) {
						af.setEsEstudiante(true);
					}

					if (af.esEstudiante()) {
						af.setTipoAfiliadoEstadoCuenta(TIPO_AFILIADO_ESTUDIANTE);
					} else if (af.esIndependiente()) {
						af.setTipoAfiliadoEstadoCuenta(TIPO_AFILIADO_INDEPENDIENTE);
					} else {
						af.setTipoAfiliadoEstadoCuenta(TIPO_AFILIADO_EMPRESA);
					}

					// recorre las coberturas encontradas, lo normal es q solo
					// sea 1,
					// y sea el mismo tipoCot entre lo reportado y la afil.
					// las coberturas vienen ordenadas x tope max ibc
					for (DatosCobertura dc : datosCobertura) {

						// Camino feliz: lo reportado es igual a la afiliacion
						// devuelve el afiliado
						if (af.getTipoCotizante().equals(dc.getCondicion().getTipoCotizante())
								&& af.getCondicion().getIbcMaximo() != null) {
							setearDatosCobertura(af, dc);
							return ReprocesoAfiliadosMsg.crear(nmconsecutivo, af);
						}

						// --> Independiente
						// si es un independiente y la cobertura es tipoAfiliado
						// = 2
						// entoncs devuelve esa
						if (af.esIndependiente()
								&& TIPO_AFILIADO_INDEPENDIENTE.equals(dc.getCondicion().getTipoAfiliado())) {
							// devuelve el primer registro q deberia ser el de
							// tope max ibc
							setearDatosCobertura(af, dc);
							break;
						}

						// --> Estudiante
						// si es un estudiante y tiene varias coberturas, la
						// planilla es N,K,U y la
						// cobertura actual
						if (af.esEstudiante()
								&& Arrays.stream(tiposCotizantesEstudiantes)
										.anyMatch(dc.getCondicion().getTipoCotizante()::equals)
								&& Arrays.stream(tiposPlanillasEstudiantes).anyMatch(lg.getTipoPlanilla()::equals)
								&& af.getCobertura().getTotalCoberturas() > 1) {

							setearDatosCobertura(af, dc);
							break;

						}

						// --> Dependiente
						// si es empresa se la aplica a cualquiera tipoAfiliado
						// = 1
						if (!af.esIndependiente()
								&& TIPO_AFILIADO_EMPRESA.equals(dc.getCondicion().getTipoAfiliado())) {
							// devuelve el primer registro q deberia ser el de
							// tope max ibc
							setearDatosCobertura(af, dc);
							break;
						}

					}

					// si no encontro coberturas
					if (af.getCondicion().getIbcMaximo() == null) {
						// el null se captura en reprocesoAfiliadoServicio para
						// reportar el error
						af.setTipoError(CatalogoErrores.COBERTURA_NO_ENCONTRADA);
						return ReprocesoAfiliadosMsg.crear(nmconsecutivo, af);
					}

					return ReprocesoAfiliadosMsg.crear(nmconsecutivo, af);
				});
	}

	private Map<String, Object> obtenerParametros(Afiliado afiliado, TipoErrorEjecucion tipoError,
			String descripcionError) {

		Map<String, Object> params = new HashMap<>();
		params.put("poliza", afiliado.getCobertura().getPoliza());
		params.put("periodoCotizacion", afiliado.getPeriodoCotizacion());
		params.put("dniAfiliado", afiliado.getDni());
		params.put("nmFormulario", afiliado.getLegalizacion().getNumeroFormulario());
		params.put("tipoAfiliado", afiliado.getTipoAfiliado());
		params.put("tipoCotizante", afiliado.getTipoCotizante());
		params.put("subTipoCotizante", afiliado.getSubtipoCotizante());
		params.put("tipoEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
		params.put("documentoEmpleador", afiliado.getDniEmpleador());
		params.put("snProporcional", afiliado.getCondicion().getIndicadorDias());
		params.put("cdTipoGeneracion", afiliado.getCondicion().getTipoGeneracion());
		params.put("cdtipoNovedad", afiliado.getCondicion().getTipoNovedad());
		params.put("cdtipoTasa", afiliado.getCondicion().getTipoTasa());
		params.put("ingresoBaseMaximoLiq", afiliado.getCondicion().getIbcMaximo());
		params.put("ingresoBaseMinimoLiq", afiliado.getCondicion().getIbcMinimo());
		params.put("cdtipoPlanilla", Objects.isNull(afiliado.getLegalizacion().getTipoPlanilla())
				? afiliado.getLegalizacion().getTipoPlanilla() : afiliado.getLegalizacion().getTipoPlanilla().name());
		params.put("cdtipoProceso", Objects.isNull(afiliado.getLegalizacion().getTipoProceso())
				? afiliado.getLegalizacion().getTipoProceso() : afiliado.getLegalizacion().getTipoProceso().name());
		params.put("cdNuevaSucursal", afiliado.getInfoVct().getCentroTrabajo());
		params.put("fechaInicioVct", afiliado.getInfoVct().getFechaInicioVCT());
		params.put("fechaFinVct", afiliado.getInfoVct().getFechaFinVCT());
		params.put("snVct", afiliado.getInfoVct().getSnvct());
		params.put("cdErrorProceso", tipoError.getEquivalencia());
		params.put("dsErrorProceso", descripcionError);
		params.put("dniIngresa", getVarEntorno().getValor("usuario.dniingresa"));

		return params;
	}

	private void setearDatosCobertura(Afiliado af, DatosCobertura dc) {
		af.getCobertura().setUltimoIbcCotizado(dc.getCobertura().getUltimoIbcCotizado());
		af.getCobertura().setSalario(dc.getCobertura().getSalario());
		af.getCondicion().setIbcMaximo(dc.getCondicion().getIbcMaximo());
		af.getCondicion().setIbcMinimo(dc.getCondicion().getIbcMinimo());
		af.getCondicion().setIndicadorDias(dc.getCondicion().getIndicadorDias());
		af.getCondicion().setTipoGeneracion(dc.getCondicion().getTipoGeneracion());
		af.getCondicion().setTipoNovedad(dc.getCondicion().getTipoNovedad());
		af.getCondicion().setTipoTasa(dc.getCondicion().getTipoTasa());
	}

	public enum TipoErrorEjecucion {

		TIMEOUT("01"), OTRO("02");

		private String equivalencia;

		TipoErrorEjecucion(String equivalencia) {
			this.equivalencia = equivalencia;
		}

		public String getEquivalencia() {
			return this.equivalencia;
		}
	}

}
