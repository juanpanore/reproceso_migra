package com.sura.arl.reproceso.accesodatos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import com.sura.arl.reproceso.modelo.DetallePago;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.TipoPlanilla;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;
import com.sura.arl.reproceso.modelo.integrador.IntegradorEsperadaAfiliado;

/**
 * @author jorge
 *
 */
@Repository
public class NovedadesDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(NovedadesDao.class);

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
    public NovedadesDao(CoberturaDao coberturaDao) {
        this.coberturaDao = coberturaDao;
    }

    /**
     * Devuelve un listado de afiliados que estén en un formulario Solo si el
     * SNPROCESADO de cada afiliado no es S
     * 
     * @param numeroFormulario
     * @return
     */
    public List<Afiliado> obtenerAfiliadosXformulario(Long numeroFormulario, Optional<String> dni,
            Optional<String> periodo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("numeroFormulario", numeroFormulario);
        String sql = "consulta.afiliadosXformulario";

        if (dni.isPresent()) {
            params.put("afiliado", dni.get());
            params.put("periodo", periodo.get());
            sql = "consulta.afiliadosXformularioXperiodo";
        }

        return getJdbcTemplate().query(getVarEntorno().getValor(sql), params, (rs, nm) -> {
            Afiliado af = new Afiliado();
            Cobertura cb = new Cobertura();
            Condicion cn = new Condicion();
            Legalizacion lg = new Legalizacion();
            InfoNovedadVCT iv = new InfoNovedadVCT();

            // datos basicos del afiliado
            af.setDni(rs.getString("dni"));
            af.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
            af.setTipoCotizante(rs.getString("tipoCotizante"));
            af.setSubtipoCotizante(rs.getString("subtipoCotizante"));

            af.setTipoDocumentoEmpleador(TipoDocumento.tipoDocumentoPorEquivalencia(rs.getString("tipoDocEmpleador")));
            af.setDniEmpleador(rs.getString("dniEmpleador"));
            af.setPeriodoCotizacion(rs.getString("periodo"));

            // datos basicos de la cobertura
            cb.setPoliza(rs.getString("NPOLIZA"));
            cb.setPeriodo(af.getPeriodoCotizacion());

            // datos de las condiciones
            // si la consulta no encontro relacion entre el tipoCot y las condiciones
            // estos valores llegan null
            cn.setIndicadorDias(rs.getString("SNINDICADOR_PROPORCIONAL_DIAS"));
            cn.setTipoGeneracion(rs.getString("CDTIPO_GENERACION"));
            cn.setTipoNovedad(rs.getString("CDTIPO_NOVEDAD"));
            cn.setTipoTasa(rs.getString("CDTIPO_TASA"));
            cn.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
            cn.setIbcMaximo(rs.getInt("PTINGRESO_MAX_BASE_LIQ"));
            cn.setIbcMinimo(rs.getInt("PTINGRESO_MIN_BASE_LIQ"));

            // datos de la legalizacion
            lg.setNumeroFormulario(numeroFormulario);
            lg.setTipoPlanilla(TipoPlanilla.valueOf(rs.getString("CDTIPO_PLANILLA")));
            lg.setTipoProceso(TipoProceso.valueOf(rs.getString("CDTIPO_PROCESO")));

            // datos de vct reportado
            iv.setCentroTrabajo(rs.getString("CT"));
            iv.setFechaFinVCT(rs.getDate("FECHAFINVCT"));
            iv.setFechaInicioVCT(rs.getDate("FECHAINICIOVCT"));
            iv.setSnvct(rs.getString("SNVCT"));

            // setea los objs del afiliado
            af.setCobertura(cb);
            af.setCondicion(cn);
            af.setLegalizacion(lg);
            af.setInfoVct(iv);

            // setea si es independiente o no, segun la informacion ya validada desde la
            // legalizacion
            if (TipoProceso.I.equals(af.getLegalizacion().getTipoProceso())) {
                af.setEsIndependiente(true);
            }

            // en caso de las variables de las condiciones esten null,
            // implica que no encontro homologado el tipoCond en TCPG_CONDICIONES_COTIZANTE
            // se reporta error
            if (cn.getTipoGeneracion() == null) {
                af.setTipoError(CatalogoErrores.COBERTURA_INVALIDA);
                return af;
            }

            // se buscan todas las coberturas
            List<DatosCobertura> datosCobertura = coberturaDao.consultarDatosCoberturaAfiliados(af.getDni(),
                    cb.getPoliza(), cb.getPeriodo());

            af.getCobertura().setTotalCoberturas(datosCobertura.size());

            // setea si es independiente o no, segun la informacion ya validada desde la
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

            // recorre las coberturas encontradas, lo normal es q solo sea 1,
            // y sea el mismo tipoCot entre lo reportado y la afil.
            // las coberturas vienen ordenadas x tope max ibc
            for (DatosCobertura dc : datosCobertura) {

                // Camino feliz: lo reportado es igual a la afiliacion
                // devuelve el afiliado
                if (af.getTipoCotizante().equals(dc.getCondicion().getTipoCotizante())
                        && af.getCondicion().getIbcMaximo() != null) {
                    setearDatosCobertura(af, dc);
                    return af;
                }

                // --> Independiente
                // si es un independiente y la cobertura es tipoAfiliado = 2
                // entoncs devuelve esa
                if (af.esIndependiente() && TIPO_AFILIADO_INDEPENDIENTE.equals(dc.getCondicion().getTipoAfiliado())) {
                    // devuelve el primer registro q deberia ser el de tope max ibc
                    setearDatosCobertura(af, dc);
                    break;
                }

                // --> Estudiante
                // si es un estudiante y tiene varias coberturas, la planilla es N,K,U y la
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
                // si es empresa se la aplica a cualquiera tipoAfiliado = 1
                if (!af.esIndependiente() && TIPO_AFILIADO_EMPRESA.equals(dc.getCondicion().getTipoAfiliado())) {
                    // devuelve el primer registro q deberia ser el de tope max ibc
                    setearDatosCobertura(af, dc);
                    break;
                }

            }

            // si no encontro coberturas
            if (af.getCondicion().getIbcMaximo() == null) {
                // el null se captura en reprocesoAfiliadoServicio para reportar el error
                af.setTipoError(CatalogoErrores.COBERTURA_NO_ENCONTRADA);
                return af;
            }

            return af;
        });
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

    public List<Afiliado> obtenerNovedadIngresoRetiroAfiliadosXformulario(Long numeroFormulario) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("numeroFormulario", numeroFormulario);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.novedades.ingreso.retiro.afiliacion"), params,
                (rs, nm) -> {
                    Afiliado af = new Afiliado();

                    af.setDni(rs.getString("dni"));
                    af.setTipoCotizante(rs.getString("tipoCotizante"));
                    af.setTieneNovedadIngreso(
                            !Objects.isNull(rs.getString("novedadIng")) && X.equals(rs.getString("novedadIng").trim()));
                    af.setTieneNovedadRetiro(
                            !Objects.isNull(rs.getString("novedadRet")) && X.equals(rs.getString("novedadRet").trim()));
                    af.setFechaInicioNovedad(rs.getDate("feinicio_novedad"));
                    af.setFechaFinNovedad(rs.getDate("fefin_novedad"));

                    Cobertura cb = new Cobertura();
                    cb.setPoliza(rs.getString("NPOLIZA"));
                    cb.setPeriodo(rs.getString("periodo"));
                    af.setCobertura(cb);
                    return af;
                });
    }
    
    /**
     * Devuelve un listado de afiliados que estén en un formulario Solo si el
     * SNPROCESADO de cada afiliado no es S
     * 
     * @param numeroFormulario
     * @return
     */
    public List<Afiliado> obtenerAfiliadoXformulario(Long numeroFormulario, IntegradorEsperadaAfiliado afiliado) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("numeroFormulario", numeroFormulario);
        params.put("numeroDocumento", afiliado.getDniAfiliado().substring(1));
        params.put("tipoDocumento", afiliado.getDniAfiliado().substring(0, 1));
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        
		
        String sql = "consulta.afiliadoXformulario";

     

        return getJdbcTemplate().query(getVarEntorno().getValor(sql), params, (rs, nm) -> {
            Afiliado af = new Afiliado();
            Cobertura cb = new Cobertura();
            Condicion cn = new Condicion();
            Legalizacion lg = new Legalizacion();
            InfoNovedadVCT iv = new InfoNovedadVCT();

            // datos basicos del afiliado
            af.setDni(rs.getString("dni"));
            af.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
            af.setTipoCotizante(rs.getString("tipoCotizante"));
            af.setSubtipoCotizante(rs.getString("subtipoCotizante"));

            af.setTipoDocumentoEmpleador(TipoDocumento.tipoDocumentoPorEquivalencia(rs.getString("tipoDocEmpleador")));
            af.setDniEmpleador(rs.getString("dniEmpleador"));
            af.setPeriodoCotizacion(rs.getString("periodo"));

            // datos basicos de la cobertura
            cb.setPoliza(rs.getString("NPOLIZA"));
            cb.setPeriodo(af.getPeriodoCotizacion());

            // datos de las condiciones
            // si la consulta no encontro relacion entre el tipoCot y las condiciones
            // estos valores llegan null
            cn.setIndicadorDias(rs.getString("SNINDICADOR_PROPORCIONAL_DIAS"));
            cn.setTipoGeneracion(rs.getString("CDTIPO_GENERACION"));
            cn.setTipoNovedad(rs.getString("CDTIPO_NOVEDAD"));
            cn.setTipoTasa(rs.getString("CDTIPO_TASA"));
            cn.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
            cn.setIbcMaximo(rs.getInt("PTINGRESO_MAX_BASE_LIQ"));
            cn.setIbcMinimo(rs.getInt("PTINGRESO_MIN_BASE_LIQ"));

            // datos de la legalizacion
            lg.setNumeroFormulario(numeroFormulario);
            lg.setTipoPlanilla(TipoPlanilla.valueOf(rs.getString("CDTIPO_PLANILLA")));
            lg.setTipoProceso(TipoProceso.valueOf(rs.getString("CDTIPO_PROCESO")));

            // datos de vct reportado
            iv.setCentroTrabajo(rs.getString("CT"));
            iv.setFechaFinVCT(rs.getDate("FECHAFINVCT"));
            iv.setFechaInicioVCT(rs.getDate("FECHAINICIOVCT"));
            iv.setSnvct(rs.getString("SNVCT"));

            // setea los objs del afiliado
            af.setCobertura(cb);
            af.setCondicion(cn);
            af.setLegalizacion(lg);
            af.setInfoVct(iv);

            // setea si es independiente o no, segun la informacion ya validada desde la
            // legalizacion
            if (TipoProceso.I.equals(af.getLegalizacion().getTipoProceso())) {
                af.setEsIndependiente(true);
            }

            // en caso de las variables de las condiciones esten null,
            // implica que no encontro homologado el tipoCond en TCPG_CONDICIONES_COTIZANTE
            // se reporta error
            if (cn.getTipoGeneracion() == null) {
                af.setTipoError(CatalogoErrores.COBERTURA_INVALIDA);
                return af;
            }

            // se buscan todas las coberturas
            List<DatosCobertura> datosCobertura = coberturaDao.consultarDatosCoberturaAfiliados(af.getDni(),
                    cb.getPoliza(), cb.getPeriodo());

            af.getCobertura().setTotalCoberturas(datosCobertura.size());

            // setea si es independiente o no, segun la informacion ya validada desde la
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

            // recorre las coberturas encontradas, lo normal es q solo sea 1,
            // y sea el mismo tipoCot entre lo reportado y la afil.
            // las coberturas vienen ordenadas x tope max ibc
            for (DatosCobertura dc : datosCobertura) {

                // Camino feliz: lo reportado es igual a la afiliacion
                // devuelve el afiliado
                if (af.getTipoCotizante().equals(dc.getCondicion().getTipoCotizante())
                        && af.getCondicion().getIbcMaximo() != null) {
                    setearDatosCobertura(af, dc);
                    return af;
                }

                // --> Independiente
                // si es un independiente y la cobertura es tipoAfiliado = 2
                // entoncs devuelve esa
                if (af.esIndependiente() && TIPO_AFILIADO_INDEPENDIENTE.equals(dc.getCondicion().getTipoAfiliado())) {
                    // devuelve el primer registro q deberia ser el de tope max ibc
                    setearDatosCobertura(af, dc);
                    break;
                }

                // --> Estudiante
                // si es un estudiante y tiene varias coberturas, la planilla es N,K,U y la
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
                // si es empresa se la aplica a cualquiera tipoAfiliado = 1
                if (!af.esIndependiente() && TIPO_AFILIADO_EMPRESA.equals(dc.getCondicion().getTipoAfiliado())) {
                    // devuelve el primer registro q deberia ser el de tope max ibc
                    setearDatosCobertura(af, dc);
                    break;
                }

            }

            // si no encontro coberturas
            if (af.getCondicion().getIbcMaximo() == null) {
                // el null se captura en reprocesoAfiliadoServicio para reportar el error
                af.setTipoError(CatalogoErrores.COBERTURA_NO_ENCONTRADA);
                return af;
            }

            return af;
        });
    }


    public void actualizarProcesado(Afiliado afiliado, boolean procesado, List<DetallePago> pagos) {

        String sql = getVarEntorno().getValor("actualizar.estadoCarga.afiliado.novedades");
        
        if (afiliado == null) {
            throw new AccesoDatosExcepcion("No es posible registrar sin datos");
        }

        Map<String, Object> params = new HashMap<String, Object>(5);
        params.put("tipoDocumento", afiliado.getDni().substring(0,1));
        params.put("numeroDocumento", afiliado.getNumeroDocumento());
        params.put("periodo", afiliado.getCobertura().getPeriodo());
        params.put("poliza", afiliado.getCobertura().getPoliza());
        params.put("estado", procesado ? S : N);
        
        //params.put("tipoAfiliado", afiliado.getTipoAfiliado());
        //params.put("tipoCotizante", afiliado.getTipoCotizante());

        pagos.forEach(p -> {
            params.put("formulario", p.getNumeroFormulario());
            getJdbcTemplate().update(sql, params);
        });

    }
 
    
    @Transactional
    public int actualizarErrorRenesNovedad(String dni, Long formulario, boolean esRene, String tipoDocumento) {

        if (Objects.isNull(dni)) {
            throw new AccesoDatosExcepcion("No es posible registrar sin dni");
        }
        
        if(Objects.isNull(formulario)) {      
            throw new AccesoDatosExcepcion("No es posible actualizar novedad sin formulario");
        }

        Map<String, Object> params = new HashMap<String, Object>(5);
        
        params.put("numeroDocumento", dni);
        //params.put("poliza", poliza);
        params.put("esRene", esRene ? 1 : 0);
        params.put("formulario", formulario);
        params.put("tipoDocumento", tipoDocumento);
        return getJdbcTemplate().update(getVarEntorno().getValor("actualizar.errorRene.novedad.afiliado"), params);

    }

    public InfoNovedadVCT getNovedadVCT(String dni, String periodo, String poliza, String tipoCotizante){
         
        Map<String, Object> params = new HashMap<>();
        params.put("afiliado", dni);
        params.put("periodo", periodo);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        
        List<InfoNovedadVCT> list = getJdbcTemplate().query(getVarEntorno().getValor("consultar.novedad.vct"), params, (rs, nm) -> {
           InfoNovedadVCT iv = new InfoNovedadVCT();
            
            // datos de vct reportado
            iv.setCentroTrabajo(rs.getString("CT"));
            iv.setFechaFinVCT(rs.getDate("FECHAFINVCT"));
            iv.setFechaInicioVCT(rs.getDate("FECHAINICIOVCT"));
            iv.setSnvct(rs.getString("SNVCT"));
            
            return iv;
        });
        
        return list.isEmpty() ? null : list.get(0);
    }
}
