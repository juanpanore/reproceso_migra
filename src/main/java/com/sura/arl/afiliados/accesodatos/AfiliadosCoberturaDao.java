package com.sura.arl.afiliados.accesodatos;

import java.sql.ResultSet;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.afiliados.modelo.Cobertura;
import com.sura.arl.afiliados.modelo.Condicion;
import com.sura.arl.afiliados.modelo.Legalizacion;
import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.util.Periodo;
import java.sql.SQLException;

@Repository
public class AfiliadosCoberturaDao extends AbstractDAO {

    private final DateTimeFormatter FORMATO_YYYY_MMM = DateTimeFormatter.ofPattern("yyyyMM");
    private static final Logger LOG = LoggerFactory.getLogger(AfiliadosCoberturaDao.class);

    private Afiliado rowMapper(ResultSet rs, int index, final Cobertura cobertura) throws SQLException {
        Cobertura cbrtr = cobertura != null ? cobertura.cloneCobertura() : new Cobertura();

        Legalizacion lg = new Legalizacion();
        Condicion condicion = new Condicion();
        condicion.setIbcMaximo(rs.getInt("PTINGRESO_MAX_BASE_LIQ"));
        condicion.setIbcMinimo(rs.getInt("PTINGRESO_MIN_BASE_LIQ"));
        condicion.setTipoGeneracion(rs.getString("CDTIPO_GENERACION"));
        condicion.setTipoTasa(rs.getString("CDTIPO_TASA"));
        condicion.setIndicadorDias(rs.getString("SNINDICADOR_PROPORCIONAL_DIAS"));
        condicion.setTipoNovedad(rs.getString("CDTIPO_NOVEDAD"));
        
        

        cbrtr.setPoliza(rs.getString("NPOLIZA"));
        cbrtr.setFealta(rs.getDate("FEALTA"));
        cbrtr.setFebaja(rs.getDate("FEBAJA"));
        try {
            cbrtr.setEsMismoPeriodoDeAlta(rs.getString("ES_MISMO_PERIODO_DE_ALTA"));
        } catch (SQLException ignore) {
        }
        try {
            cbrtr.setPeriodoEsMenorFealta(rs.getString("PERIODO_MENOR_FEALTA"));
        } catch (SQLException ignore) {
        }
        try{lg.setNumeroFormulario(rs.getLong("NMFORMULARIO_PAGO"));}catch(SQLException ignore){}
        try {
            cbrtr.setPeriodo(rs.getString("XPERIODO"));
        } catch (SQLException ignore) {
        }

        Afiliado afiliado = new Afiliado();
        afiliado.setDni(rs.getString("DNI"));
        afiliado.setTipoAfiliado(rs.getString("CDTIPO_AFILIADO"));
        afiliado.setTipoCotizante(rs.getString("CDTIPO_COTIZANTE"));
        afiliado.setCertificado(rs.getString("NCERTIFICADO"));
        afiliado.setSalario(rs.getInt("PTSALARIO"));
        afiliado.setTipoError(rs.getString("ERROR"));
        afiliado.setUltimoIbc(rs.getDouble("PTULTIMO_IBC_COTIZADO"));
        if(rs.getString("TIPODOC_EMPLEADOR")!=null) {
            afiliado.setTipoDocumentoEmpleador(TipoDocumento.tipoDocumentoPorEquivalencia(rs.getString("TIPODOC_EMPLEADOR")));
        }
        afiliado.setDniEmpleador(rs.getString("DNI_EMPLEADOR"));

        afiliado.setSubtipoCotizante(rs.getString("CDSUBTIPO_COTIZANTE"));
        afiliado.setNmroCoberturas(rs.getInt("NMRO_COBERTURAS"));
        
        afiliado.setCobertura(cbrtr);
        afiliado.setCondicion(condicion);
        afiliado.setLegalizacion(lg);
        afiliado.setCsvTiposCotizantes(rs.getString("CSVTIPOS_COTIZANTES"));
        
        return afiliado;
    }

    public List<Afiliado> consultarAfiliados(Cobertura cobertura, Optional<String> dni) {

        Map<String, Object> params = new HashMap<>();
        params.put("periodoGeneracion", cobertura.getPeriodoGeneracion());
        params.put("poliza", cobertura.getPoliza());
        params.put("dni", dni.orElse(null));

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.cobertura.afiliados"), params,
                (ResultSet rs, int index) -> rowMapper(rs, index, cobertura));
    }

    public Afiliado consultarAfiliado(Cobertura cobertura, String dniAfiliado, String certificado) {

        Map<String, Object> params = new HashMap<>();
        params.put("periodoGeneracion", cobertura.getPeriodoGeneracion());
        params.put("poliza", cobertura.getPoliza());
        params.put("dniAfiliado", dniAfiliado);
        params.put("certificado", certificado);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.cobertura.afiliado"), params,
                (ResultSet rs, int index) -> rowMapper(rs, index, cobertura));
    }

    public Afiliado consultarAfiliado(String poliza, String dniAfiliado, String tipoAfiliado, String tipoCotizante,
            String nmCertificado) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dni", dniAfiliado);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("certificado", nmCertificado);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.integrador.afiliado"), params,
                (ResultSet rs, int index) -> rowMapper(rs, index, null));
    }
    
    public List<Afiliado> consultarAfiliadosReversados(String numeroFormulario){
        return consultarAfiliadosReversados(numeroFormulario, null, null, null);
    }

    public List<Afiliado> consultarAfiliadosReversados(String numeroFormulario,String dniAfiliado, String periodoBorrado, String tipoDocumento) {

        Map<String, Object> params = new HashMap<>();
        params.put("numeroFormulario", numeroFormulario);
        params.put("afiliado", dniAfiliado);
        params.put("periodo", periodoBorrado);
        params.put("tipoDocumento", tipoDocumento);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.integrador.afiliados.reversados"), params,
                (ResultSet rs, int index) -> {

                    Afiliado afiliado = rowMapper(rs, index, null);
                    
                    Periodo periodo = Periodo.parse(rs.getString("NMPERIODO"), "yyyyMM");
                    
                    afiliado.getCondicion().setPeriodoCotizacion(periodo.toString());
                    afiliado.getCobertura().setPeriodoGeneracion(Periodo.now().toString());
                    afiliado.getCobertura().setPeriodo(periodo.format("MMyyyy"));
                    afiliado.setPeriodoCotizacion(periodo.toString());

                    return afiliado;

                });
    }
    
    public List<Afiliado> consultarAfiliadosNoReversados(String numeroFormulario,String dniAfiliado, String periodoBoorado, String tipoDocumento) {

        Map<String, Object> params = new HashMap<>();
        params.put("numeroFormulario", numeroFormulario);
        params.put("afiliado", dniAfiliado);
        params.put("tipoDocumento", tipoDocumento);
        params.put("periodo", periodoBoorado);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.integrador.afiliados.noreversados"), params,
                (ResultSet rs, int index) -> {

                    Afiliado afiliado = rowMapper(rs, index, null);
                    
                    Periodo periodo = Periodo.parse(rs.getString("NMPERIODO"), "yyyyMM");
                    
                    afiliado.getCondicion().setPeriodoCotizacion(periodo.toString());
                    afiliado.getCobertura().setPeriodoGeneracion(Periodo.now().toString());
                    afiliado.getCobertura().setPeriodo(periodo.format("MMyyyy"));
                    afiliado.setPeriodoCotizacion(periodo.toString());

                    return afiliado;

                });
    }
    
    public List<Afiliado> consultarAfiliadosFlujoCompleto(String numeroFormulario,String dniAfiliado, String tipoDocumento, String periodoCotizacion) {

        Map<String, Object> params = new HashMap<>();
        params.put("numeroFormulario", numeroFormulario);
        params.put("afiliado", dniAfiliado);
        params.put("tipoDocumento", tipoDocumento);
        params.put("periodo", periodoCotizacion);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.integrador.afiliados.flujo.completo"), params,
                (ResultSet rs, int index) -> {

                    Afiliado afiliado = rowMapper(rs, index, null);
                    
                    Periodo periodo = Periodo.parse(rs.getString("NMPERIODO"), "yyyyMM");
                    
                    afiliado.getCondicion().setPeriodoCotizacion(periodo.toString());
                    afiliado.setPeriodoCotizacion(periodo.toString());
                    afiliado.getCobertura().setPeriodoGeneracion(Periodo.now().toString());
                    afiliado.getCobertura().setPeriodo(periodo.format("MMyyyy"));
                    afiliado.setPeriodoCotizacion(periodo.toString());
                   // afiliado.getLegalizacion().setNumeroFormulario(Long.valueOf(rs.getString("NMFORMULARIO_PAGO")));

                    return afiliado;

                });
    }

    public List<Afiliado> consultarAfiliadoPorCentroTrabajo(String poliza, String sucursal, String actividad,
            String clase) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("sucursal", sucursal);
        params.put("actividad", actividad);
        params.put("clase", clase);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.integrador.afiliado.centroTrabajo"), params,
                (ResultSet rs, int index) -> rowMapper(rs, index, null));
    }

    public List<String> consultarPeriodosReCalcularEstadoCuenta(String poliza, String dniAfiliado, String tipoAfiliado,
            String tipoCotizante, String periodoFealta, String periodoFebaja) {

        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dni", dniAfiliado);
        params.put("periodoFealta", periodoFealta);
        params.put("periodoFebaja", periodoFebaja);
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("tipoCotizante", tipoCotizante);
        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.rangoPeriodos.borrarEstadoCuenta"),
                    params, (ResultSet rs, int index) -> {

                        List<String> periodos = new ArrayList<>();

                        YearMonth periodoInicial = YearMonth.parse(rs.getString("PERIODO_INICIAL"), FORMATO_YYYY_MMM);
                        YearMonth periodoFinal = YearMonth.parse(rs.getString("PERIODO_FINAL"), FORMATO_YYYY_MMM);

                        while (periodoInicial.isBefore(periodoFinal) || periodoInicial.equals(periodoFinal)) {

                            periodos.add(periodoInicial.format(FORMATO_YYYY_MMM));
                            periodoInicial = periodoInicial.plusMonths(1);
                        }

                        return periodos;
                    });
        } catch (EmptyResultDataAccessException ignore) {
            return new ArrayList<>();
        }
    }

    public Optional<Condicion> consultarCondicionesCotizante(String tipoCotizante, String tipoAfiliado) {

        Map<String, Object> params = new HashMap<>();
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("tipoCotizante", tipoCotizante);

        try {
            Condicion condiciones = getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.condiciones.tipoCotizante"), params,
                    (ResultSet rs, int index) -> {

                        Condicion condicion = new Condicion();
                        condicion.setIbcMaximo(rs.getInt("PTINGRESO_MAX_BASE_LIQ"));
                        condicion.setIbcMinimo(rs.getInt("PTINGRESO_MIN_BASE_LIQ"));
                        condicion.setTipoGeneracion(rs.getString("CDTIPO_GENERACION"));
                        condicion.setTipoTasa(rs.getString("CDTIPO_TASA"));
                        condicion.setIndicadorDias(rs.getString("SNINDICADOR_PROPORCIONAL_DIAS"));

                        return condicion;
                    });

            return Optional.of(condiciones);
        } catch (EmptyResultDataAccessException erdae) {
            LOG.info("Se se encontraron condiciones cotizante para tipo afiliado {}, tipo cotizante {} ", tipoAfiliado, tipoCotizante);
        }
        return Optional.empty();
    }

    public List<Afiliado> consultarAfiliado(String poliza, String dni) {
        Map<String, Object> params = new HashMap<>();
        params.put("poliza", poliza);
        params.put("dni", dni);
        params.put("tipoAfiliado", null);
        params.put("tipoCotizante", null);
        params.put("certificado", null);

        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.integrador.afiliado"), params,
                (ResultSet rs, int index) -> rowMapper(rs, index, null));
    }

//    public List<Afiliado> consultarAfiliadoNMFormulario(String dni) {
//        Map<String, Object> params = new HashMap<>();
//        params.put("dni", dni);
//
//        return getJdbcTemplate().query(getVarEntorno().getValor("consulta.integrador.afiliados.nmformulario"), params,
//                (ResultSet rs, int index) -> rowMapper(rs, index, null));
//    }
    
    public String getTipoCotizanteAnulado(String poliza,String tipoAfiliado, String certificado,String afiliado){
        
        Map<String, Object> params = new HashMap<>();
        params.put("tipoAfiliado", tipoAfiliado);
        params.put("certificado", certificado);
        params.put("poliza", poliza);
        params.put("afiliado", afiliado);
        
        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consultar.tipoCotizante.borrado"), params,
                String.class);
    }
}
