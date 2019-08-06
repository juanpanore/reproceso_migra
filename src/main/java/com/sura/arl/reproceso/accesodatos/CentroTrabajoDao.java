package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Repository;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.modelo.EstadoCuenta;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class CentroTrabajoDao extends AbstractDAO {

    public DatosCentroTrabajo consultarDatosCT(String periodo, String poliza, String sucursal) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        params.put("sucursal", sucursal);

        try{
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.datosCT"), params,
                (ResultSet rs, int index) -> {
                    DatosCentroTrabajo pd = new DatosCentroTrabajo();
                    pd.setTasa(rs.getDouble("POCOTIZACION_ATEP"));
                    pd.setSucursalActualizar(rs.getString("CDSUCURSAL"));
                    pd.setFealta(rs.getDate("FEALTA"));
                    pd.setFebaja(rs.getDate("FEBAJA"));
                    return pd;
                });
        }catch(DataAccessException e) {
            return null;
        }
    }

    public RespuestaFechasCobertura consultarFechasCobertura(String periodo, EstadoCuenta estadoCuenta, Afiliado afiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("poliza", afiliado.getCobertura().getPoliza());
        params.put("dni", afiliado.getDni());
        params.put("tipoCotizante", estadoCuenta.getAfiliado().getTipoCotizante());
        params.put("tipoAfiliado", estadoCuenta.getAfiliado().getTipoAfiliado());

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.maxFechasCobertura"), params,
                (ResultSet rs, int index) -> {
                    RespuestaFechasCobertura pd = new RespuestaFechasCobertura();
                    pd.setMaxFealta(rs.getDate("MAX_FEALTA"));
                    pd.setMaxFebaja(rs.getDate("MAX_FEBAJA"));
                    pd.setUltimoPeriodocotizado(rs.getString("NMULTIMO_PERIODO_COTIZADO"));
                    pd.setTotalCoberturas(rs.getInt("TOTAL_COBERTURAS"));
                    return pd;
                });
    }

    public DatosCobertura consultarDatosCobertura(String periodo, String poliza, String dni, String tipoCotizante,
            String tipoAfiliado) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("poliza", poliza);
        params.put("dni", dni);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoAfiliado", tipoAfiliado);
        
        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.datosCobertura"), params,
                (ResultSet rs, int index) -> {
                    DatosCobertura pd = new DatosCobertura();
                    pd.setFebaja(rs.getDate("FEBAJA"));
                    pd.setCertificado(rs.getLong("NCERTIFICADO"));
                    return pd;
                });
    }

    public Date buscarProximaFechaVCT(String periodo, String dniEmpleado, String tipoCotizante) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);
        params.put("dniEmpleado", dniEmpleado);
        params.put("tipoCotizante", tipoCotizante);

        return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.proximaFechaVCT"), params,
                Date.class);
    }

    public static class EjecutarCambioCTDao extends StoredProcedure {

        private static final String QUERY = "pkgafi_cambios_ctp.pcnafi_validaciones_cambio_ct";

        public EjecutarCambioCTDao(DataSource dataSource) {
            super(dataSource, QUERY);

            declareParameter(new SqlParameter("npoliza", Types.VARCHAR));
            declareParameter(new SqlParameter("dniEmpleado", Types.VARCHAR));
            declareParameter(new SqlParameter("periodo", Types.VARCHAR));
            declareParameter(new SqlParameter("proximoVct", Types.VARCHAR));
            declareParameter(new SqlParameter("sucursal", Types.VARCHAR));
            declareParameter(new SqlParameter("feBajaAfiliado", Types.VARCHAR));
            declareParameter(new SqlParameter("fuenteCambio", Types.VARCHAR));
            declareParameter(new SqlParameter("dniUsuario", Types.VARCHAR));
            declareParameter(new SqlParameter("certificado", Types.VARCHAR));
            declareParameter(new SqlOutParameter("error", Types.INTEGER));
            declareParameter(new SqlOutParameter("msg", Types.VARCHAR));
        }

        public Object execute(String npoliza, String dniEmpleado, String periodo, String proximoVct, String sucursal,
                Date febajaAfiliado, String fuenteCambio, String dniUsuario, String ceretificado) {

            Map<String, Object> results = super.execute(npoliza, dniEmpleado, periodo, proximoVct, sucursal,
                    febajaAfiliado, fuenteCambio, dniUsuario, ceretificado);
            return results.get("verror");
        }
    }

    public String ejecutarCambioCT(String npoliza, String dniEmpleado, String periodo, String proximoVct,
            String sucursal, Date febajaAfiliado, String fuenteCambio, String dniUsuario, String ceretificado) {

        EjecutarCambioCTDao ejecutarReversaNotasDao = new EjecutarCambioCTDao(getDataSource());

        Object results = ejecutarReversaNotasDao.execute(npoliza, dniEmpleado, periodo, proximoVct, sucursal,
                febajaAfiliado, fuenteCambio, dniUsuario, ceretificado);

        return (String) results;

    }

    public class DatosCentroTrabajo {
        private Double tasa;
        private String sucursalActualizar;
        private Date fealta;
        private Date febaja;

        public Double getTasa() {
            return tasa;
        }

        public void setTasa(Double tasa) {
            this.tasa = tasa;
        }

        public String getSucursalActualizar() {
            return sucursalActualizar;
        }

        public void setSucursalActualizar(String sucursalActualizar) {
            this.sucursalActualizar = sucursalActualizar;
        }

        public Date getFealta() {
            return fealta;
        }

        public void setFealta(Date fealta) {
            this.fealta = fealta;
        }

        public Date getFebaja() {
            return febaja;
        }

        public void setFebaja(Date febaja) {
            this.febaja = febaja;
        }

    }

    public class RespuestaFechasCobertura {
        private Date maxFealta;
        private Date maxFebaja;
        private String ultimoPeriodocotizado;
        private Integer totalCoberturas;

        public Date getMaxFeaLta() {
            return maxFealta;
        }

        public void setMaxFealta(Date maxFealta) {
            this.maxFealta = maxFealta;
        }

        public Date getMaxFebaja() {
            return maxFebaja;
        }

        public void setMaxFebaja(Date maxFebaja) {
            this.maxFebaja = maxFebaja;
        }

        public String getUltimoPeriodocotizado() {
            return ultimoPeriodocotizado;
        }

        public void setUltimoPeriodocotizado(String ultimoPeriodocotizado) {
            this.ultimoPeriodocotizado = ultimoPeriodocotizado;
        }

        public Integer getTotalCoberturas() {
            return totalCoberturas;
        }

        public void setTotalCoberturas(Integer totalCoberturas) {
            this.totalCoberturas = totalCoberturas;
        }

    }

    public class DatosCobertura {
        private Date febaja;
        private Long certificado;

        public Date getFebaja() {
            return febaja;
        }

        public void setFebaja(Date febaja) {
            this.febaja = febaja;
        }

        public Long getCertificado() {
            return certificado;
        }

        public void setCertificado(Long certificado) {
            this.certificado = certificado;
        }
    }

    public class RespuestaUltimoPeriodoCotizado {
        private String ultimoPeriodoCotizado;
        private String ctp;
        private Long cotizacion;

        public String getUltimoPeriodoCotizado() {
            return ultimoPeriodoCotizado;
        }

        public void setUltimoPeriodoCotizado(String ultimoPeriodoCotizado) {
            this.ultimoPeriodoCotizado = ultimoPeriodoCotizado;
        }

        public String getCtp() {
            return ctp;
        }

        public void setCtp(String ctp) {
            this.ctp = ctp;
        }

        public Long getCotizacion() {
            return cotizacion;
        }

        public void setCotizacion(Long cotizacion) {
            this.cotizacion = cotizacion;
        }
    }

}
