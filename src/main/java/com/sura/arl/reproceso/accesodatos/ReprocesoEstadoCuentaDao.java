package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.ConsolidadoNovedades;
import com.sura.arl.reproceso.modelo.DatosNovedades;
import com.sura.arl.reproceso.modelo.InfoNovedadVCT;
import com.sura.arl.reproceso.modelo.TipoPlanilla;

@Repository
public class ReprocesoEstadoCuentaDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ReprocesoEstadoCuentaDao.class);

    static final int NUMERO_REGISTROS_TRANSACCION = 2000;
    static final String S = "S";
    static final String N = "N";
    static final String X = "X";

    @Autowired
    public ReprocesoEstadoCuentaDao(PlatformTransactionManager transactionManager) {
    }

    /**
     * Devuelve un listado de leyes que reportadas en la cadena de formularios
     * 
     * @param periodo
     * @param cadenaFormularios
     * @return
     */
    public List<String> obtenerLeyNovedades(String periodo, String cadenaFormularios) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("cadenaFormularios", cadenaFormularios);

        try {
            return getJdbcTemplate().query(getVarEntorno().getValor("consultar.ley.proceso"), params,
                    (ResultSet rs, int index) -> {
                        return rs.getString("CDLEY");
                    });
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Busca todos los formularios (independientes) donde se encuentre el afiliado
     * (con tipoCotizante y empleador) en un periodo, y la cadena de formularios,
     * devuelve una cadena con los formularios concatenados
     * 
     * @param periodo
     * @param afiliado
     * @param cadenaFormularios
     * @return
     */
    @Deprecated
    public String obtenerContratosIndependientes(String periodo, String cadenaFormularios, Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("cadenaFormularios", cadenaFormularios);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("tipoDocumento", afiliado.getDni().substring(0,1));
        params.put("dniEmpleador", afiliado.getDniEmpleador());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consultar.contratos.independientes"),
                    params, String.class);
        } catch (EmptyResultDataAccessException e) {
            return "";
        }
    }

    /**
     * Busca todos los formularios donde se encuentre el afiliado (con tipoCotizante
     * y empleador) en un periodo, devuelve una cadena con los formularios
     * concatenados
     * 
     * @param periodo
     * @param afiliado
     * @return
     */
    @Deprecated
    public String obtenerContratosXAfiliado(String periodo, Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("dniEmpleador", afiliado.getDniEmpleador());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consultar.contratos.afiliado"), params,
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return "";
        }
    }

    /**
     * Listado de novedades del afiliado, que esten en los formularios de
     * cadenaFormularios que no tengan novedades: SLN,IGE,LMA,VAC,CXS,IRP>0 y ley
     * sea 2388, agrega las novedades donde la cotizacion > 0, y la ley sea 1747,
     * agrega las novedades donde la cotizacion > 0 que esten en los formularios de
     * cadenaIndependientes
     * 
     * @param periodo
     * @param afiliado
     * @param cadenaFormularios
     * @param cadenaIndependientes
     * @return
     */
    @Deprecated
    public List<DatosNovedades> consultarSinNovedades1747(String periodo, Afiliado afiliado, String cadenaFormularios,
            String cadenaIndependientes) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("cadenaFormularios", cadenaFormularios);
        params.put("cadenaIndependientes", cadenaIndependientes);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("dniEmpleador", afiliado.getDniEmpleador());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());

        return getJdbcTemplate().query(getVarEntorno().getValor("consultar.sin.novedades.1747"), params, (rs, nm) -> {
            return mapeoDatosNovedades(rs);
        });
    }

    /**
     * Listado de novedades del afiliado, que esten en los formularios de
     * cadenaFormularios que tengan novedades: SLN,IGE,LMA,VAC,CXS,IRP>0
     * 
     * @param periodo
     * @param afiliado
     * @param cadenaFormularios
     * @return
     */
    public List<DatosNovedades> consultarConNovedades(String periodo, Afiliado afiliado, String cadenaFormularios) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("cadenaFormularios", cadenaFormularios);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("dniEmpleador", afiliado.getDniEmpleador());
        // params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());

        return getJdbcTemplate().query(getVarEntorno().getValor("consultar.con.novedades.N"), params, (rs, nm) -> {
            return mapeoDatosNovedades(rs);
        });
    }

    /**
     * Listado de novedades del afiliado, que esten en los formularios de
     * cadenaFormularios que no tengan novedades: SLN,IGE,LMA,VAC,CXS,IRP>0, tambien
     * agrega las novedades donde la cotizacion > 0, y la ley sea 2388
     * 
     * @param periodo
     * @param afiliado
     * @param cadenaFormularios
     * @return
     */
    @Deprecated
    public List<DatosNovedades> consultarSinNovedades(String periodo, Afiliado afiliado, String cadenaFormularios) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("cadenaFormularios", cadenaFormularios);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("dniEmpleador", afiliado.getDniEmpleador());
        // params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());

        return getJdbcTemplate().query(getVarEntorno().getValor("consultar.sin.novedades.N"), params, (rs, nm) -> {
            return mapeoDatosNovedades(rs);
        });
    }

    private DatosNovedades mapeoDatosNovedades(ResultSet rs) throws SQLException {
        DatosNovedades r = new DatosNovedades();

        r.setCotizacion(rs.getLong("PTCOTIZACION"));
        r.setDias(rs.getDouble("NMDIAS_COTIZADOS"));
        r.setFechaPago(rs.getDate("FEPAGO"));
        r.setIbc(rs.getDouble("PTINGRESO_BASE_LIQ"));
        r.setIngreso(N.equals(rs.getString("SNING").trim()) ? false : true);
        r.setRetiro(N.equals(rs.getString("SNRET").trim()) ? false : true);
        r.setTasa(rs.getDouble("POCOTIZACION_ATEP"));
        r.setTipoPlanilla(TipoPlanilla.valueOf(rs.getString("CDTIPO_PLANILLA")));
        r.setPlanilla(rs.getString("NMPLANILLA"));
        r.setResponsable(rs.getString("RESPONSABLE"));
        r.setNumeroFormulario(rs.getLong("NMFORMULARIO_PAGO"));
        r.setTipoCotizante(rs.getString("TIPO_COTIZANTE"));
        r.setSubTipoCotizante(rs.getString("CDSUBTIPO_COTIZANTE"));
        r.setPeriodo(rs.getString("NMPERIODO"));
        return r;
    }

    public ConsolidadoNovedades consultarNovedadesAfiliado(Afiliado afiliado, String periodo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("tipoDocEmpleado", afiliado.getDni().substring(0,1));
        params.put("dniEmpleador", afiliado.getDniEmpleador());
        params.put("tipoCotizante", afiliado.getTipoCotizante());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());

        List<ObjNovedad> ln = getJdbcTemplate().query(getVarEntorno().getValor("consultar.novedades.afiliado"), params,
                (rs, nm) -> {
                    return new ObjNovedad(rs.getLong("NMFORMULARIO_PAGO"), rs.getString("NPOLIZA"), rs.getString("DNI"),
                            rs.getString("NMPERIODO"),
                            TipoDocumento.tipoDocumentoPorEquivalencia(rs.getString("TIPO_DOCUMENTO_EMPLEADOR")),
                            rs.getString("DNI_EMPLEADOR"), rs.getString("CDLEY"), rs.getString("CDTIPO_AFILIADO"),
                            TipoPlanilla.valueOf(rs.getString("CDTIPO_PLANILLA")), rs.getString("SNSLN"),
                            rs.getString("SNIGE"), rs.getString("SNLMA"), rs.getString("SNVAC"), rs.getString("SNCXS"),
                            rs.getString("NMIRP"), rs.getDouble("NMDIAS_COTIZADOS"), rs.getDouble("POCOTIZACION_ATEP"),
                            rs.getLong("PTCOTIZACION"), rs.getDouble("PTINGRESO_BASE_LIQ"), rs.getString("SNING"),
                            rs.getString("SNRET"), rs.getString("NMPLANILLA"), rs.getDate("FEPAGO"),
                            rs.getString("RESPONSABLE"), rs.getString("CDSUBTIPO_COTIZANTE"),
                            rs.getString("TIPO_COTIZANTE"), rs.getString("CT"), rs.getDate("FECHAINICIOVCT"),
                            rs.getDate("FECHAFINVCT"), rs.getString("SNVCT"), rs.getString("TIPONOVEDAD"),
                            rs.getLong("NMCONSECUTIVO"), rs.getString("ID"));
                });

        ConsolidadoNovedades cn = new ConsolidadoNovedades();
        List<DatosNovedades> laboradas = new ArrayList<>();
        List<DatosNovedades> ausentismo = new ArrayList<>();
        List<InfoNovedadVCT> listaVct = new ArrayList<>();
        boolean tiene1747 = false;
        List<Long> formulariosAfectados = new ArrayList<>();
        
        
        List<DatosNovedades> laboradasOriginal = new ArrayList<>();
        List<DatosNovedades> ausentismoOriginal = new ArrayList<>();

        tiene1747 = ln.stream().anyMatch(n -> n.cdley.equals("1747"));
        
        ln.forEach(obj -> {
            DatosNovedades dnOriginal = mapearNovedad(obj);
            
            obj = validarPlanillaN(obj);

            DatosNovedades dn = mapearNovedad(obj);

            if ("X".equals(obj.vct)) {
                InfoNovedadVCT ivct = new InfoNovedadVCT();
                ivct.setCentroTrabajo(obj.nuevoCT);
                ivct.setFechaFinVCT(obj.finCT);
                ivct.setFechaInicioVCT(obj.inicioVCT);
                ivct.setSnvct(obj.vct);
                listaVct.add(ivct);
            }

            if (!formulariosAfectados.contains(obj.numeroFormulario)) {
                formulariosAfectados.add(obj.numeroFormulario);
            }

            if ("A".equals(obj.tipoNovedad)) {
                ausentismo.add(dn);
                ausentismoOriginal.add(dnOriginal);
            } else {
                laboradas.add(dn);
                laboradasOriginal.add(dnOriginal);
            }

            /*if ("1747".equals(obj.cdley)) {
                tiene1747 = true;
            }*/
        });

        cn.setAusentismo(ausentismo);
        cn.setAusentismoOriginal(ausentismoOriginal);
        cn.setLaboradas(laboradas);
        cn.setLaboradasOriginal(laboradasOriginal);
        cn.setFormulariosAfectados(formulariosAfectados);
        cn.setListaVct(listaVct);
        cn.setLey(tiene1747 ? "1747" : "2388");
        return cn;

    }
    
    private DatosNovedades mapearNovedad(ObjNovedad obj) {
        DatosNovedades dn = new DatosNovedades();
        dn.setCotizacion(obj.cotizacion);
        dn.setDias(obj.diasCotizados);
        dn.setFechaPago(obj.fepago);
        dn.setIbc(obj.ibc);
        dn.setIngreso(N.equals(obj.ingreso.trim()) ? false : true);
        dn.setRetiro(N.equals(obj.retiro.trim()) ? false : true);
        dn.setTasa(obj.tasa);
        dn.setTipoPlanilla(obj.tipoPlanilla);
        dn.setPlanilla(obj.planilla);
        dn.setResponsable(obj.responsable);
        dn.setNumeroFormulario(obj.numeroFormulario);
        dn.setTipoCotizante(obj.tipoCotizante);
        dn.setSubTipoCotizante(obj.subtipoCotizante);
        dn.setPeriodo(obj.nmperiodo);
        dn.setConsecutivo(obj.consecutivo);
        dn.setId(obj.id); 
        dn.setLey(obj.cdley);
        return dn;
    }
    
    private ObjNovedad validarPlanillaN(ObjNovedad obj) {

        if (TipoPlanilla.N.equals(obj.tipoPlanilla)) {

            // busca la info en la tabla de afectacion planillas N
            Optional<DatosPlanillaPadre> datosPadre = obtenerDatosPlanillaPadre(obj.numeroFormulario, obj.dni,
                    obj.tipoCotizante);

            if (datosPadre.isPresent()) {
                if (!datosPadre.get().esModificacion) {
                    // obj.ibc += datosPadre.get(0).ibc;
                    // obj.cotizacion += datosPadre.get(0).cotizacion;
                    obj.tasa += datosPadre.get().tasa;
                }
            }

            // si no encontro nada, se tomaria el obj y se enviar normal
        }

        return obj;

    }

    private Optional<DatosPlanillaPadre> obtenerDatosPlanillaPadre(Long numeroFormularioHijo, String dni,
            String tipoCotizante) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("numeroFormularioHijo", numeroFormularioHijo);
        params.put("dni", dni.replaceAll("[^0-9.]", ""));
        // params.put("tipoCotizante", tipoCotizante);

        try {
            return getJdbcTemplate().queryForObject(
                    getVarEntorno().getValor("consulta.datos.afectacion.planilla.padre"), params,
                    (ResultSet rs, int index) -> {

                        DatosPlanillaPadre dp = new DatosPlanillaPadre();
                        dp.cotizacion = rs.getLong("COTIZACION");
                        dp.diasCotizados = rs.getDouble("DIAS");
                        dp.ibc = rs.getDouble("IBC");
                        dp.tasa = rs.getDouble("TASA");
                        dp.esModificacion = "S".equals(rs.getString("ES_MODIFICACION")) ? true : false;
                        return Optional.of(dp);
                    });
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Deprecated
    public String obtenerFormulariosHijos(Long formulario, String tipoProceso) {

        EjecutarBuscarHijos ejecutarBuscarHijos = new EjecutarBuscarHijos(getDataSource());
        Object results = ejecutarBuscarHijos.execute(formulario, tipoProceso);
        return (String) results;

    }

    @Deprecated
    public Double consultarTasaSinAusentismoPlanillaN(String periodo, Afiliado afiliado, String cadenaFormularios) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("cadenaFormularios", cadenaFormularios);
        params.put("tipoDocEmpleador", afiliado.getTipoDocumentoEmpleador().getEquivalencia());
        params.put("dniAfiliado", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("nroDocEmpleador", afiliado.getDniEmpleador());
        params.put("tipoCotizante", afiliado.getTipoCotizante());

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.maxTasa.planillasN"), params,
                Double.class);
    }

    @Deprecated
    public static class EjecutarBuscarHijos extends StoredProcedure {

        private static final String QUERY = "PKGAFI_COMUNES_OPERACIONES.FN_CONCATENAR_FORMULARIO";

        public EjecutarBuscarHijos(DataSource dataSource) {
            super(dataSource, QUERY);

            declareParameter(new SqlParameter("formulario", Types.NUMERIC));
            declareParameter(new SqlParameter("invaTipoProceso", Types.VARCHAR));
            declareParameter(new SqlOutParameter("cadena", Types.VARCHAR));
        }

        public Object execute(Long formulario, String tipoProceso) {
            Map<String, Object> results = super.execute(formulario, tipoProceso);
            return results.get("lvaFormularios");
        }
    }

    public class DatosPlanillaPadre {
        Long numeroFormulario;
        String planilla;
        Date fepago;
        Double diasCotizados;
        Double tasa;
        Long cotizacion;
        Double ibc;
        String tipoAjuste;
        boolean esModificacion;
    }

    public class ObjNovedad {
        Long numeroFormulario;
        String nmpoliza;
        String dni;
        String nmperiodo;
        TipoDocumento tipoDocumentoEmpleador;
        String dniEmpleador;
        String cdley;
        String tipoAfiliado;
        TipoPlanilla tipoPlanilla;
        String sln;
        String ige;
        String lma;
        String vac;
        String cxs;
        String irp;
        Double diasCotizados;
        Double tasa;
        Long cotizacion;
        Double ibc;
        String ingreso;
        String retiro;
        String planilla;
        Date fepago;
        String responsable;
        String subtipoCotizante;
        String tipoCotizante;
        String nuevoCT;
        Date inicioVCT;
        Date finCT;
        String vct;
        String tipoNovedad;
        Long consecutivo;
        String id;

        public ObjNovedad(Long numeroFormulario, String nmpoliza, String dni, String nmperiodo,
                TipoDocumento tipoDocumentoEmpleador, String dniEmpleador, String cdley, String tipoAfiliado,
                TipoPlanilla tipoPlanilla, String sln, String ige, String lma, String vac, String cxs, String irp,
                Double diasCotizados, Double tasa, Long cotizacion, Double ibc, String ingreso, String retiro,
                String planilla, Date fepago, String responsable, String subtipoCotizante, String tipoCotizante,
                String nuevoCT, Date inicioVCT, Date finCT, String vct, String tipoNovedad, Long consecutivo,
                String id) {
            super();
            this.numeroFormulario = numeroFormulario;
            this.nmpoliza = nmpoliza;
            this.dni = dni;
            this.nmperiodo = nmperiodo;
            this.tipoDocumentoEmpleador = tipoDocumentoEmpleador;
            this.dniEmpleador = dniEmpleador;
            this.cdley = cdley;
            this.tipoAfiliado = tipoAfiliado;
            this.tipoPlanilla = tipoPlanilla;
            this.sln = sln;
            this.ige = ige;
            this.lma = lma;
            this.vac = vac;
            this.cxs = cxs;
            this.irp = irp;
            this.diasCotizados = diasCotizados;
            this.tasa = tasa;
            this.cotizacion = cotizacion;
            this.ibc = ibc;
            this.ingreso = ingreso;
            this.retiro = retiro;
            this.planilla = planilla;
            this.fepago = fepago;
            this.responsable = responsable;
            this.subtipoCotizante = subtipoCotizante;
            this.tipoCotizante = tipoCotizante;
            this.nuevoCT = nuevoCT;
            this.inicioVCT = inicioVCT;
            this.finCT = finCT;
            this.vct = vct;
            this.tipoNovedad = tipoNovedad;
            this.consecutivo = consecutivo;
            this.id = id;
        }

    }

}
