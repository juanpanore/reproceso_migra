package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.afiliados.modelo.TipoDocumento;
import com.sura.arl.general.accesodatos.AbstractDAO;

@Repository
public class UltimosValoresCotizadosDao extends AbstractDAO {

    private static final int TRES = 3;
    private static final String LEY_1747 = "1747";
    private static final Logger LOG = LoggerFactory.getLogger(UltimosValoresCotizadosDao.class);

    /**
     * 
     * @param tipoDocumentoEmpleador tipo de documento del empleador
     * @param nroDocumentoEmpleador  numero de documento del empleador
     * @param dniAfiliado            numero de documento del afiliado, es decir, sin
     *                               el tipo de documento
     * @param tipoCotizante          tipo de cotizante del afiliado
     * @return
     */
    public RespuestaUltimoPeriodoCotizado obtenerUltimoPeriodoCotizado(TipoDocumento tipoDocumentoEmpleador,
            String nroDocumentoEmpleador, String dniAfiliado, String tipoCotizante, String tipoDocumento) {
        String sql = getVarEntorno().getValor("consulta.ultimoPeriodoCotizado");

        Map<String, Object> params = new HashMap<String, Object>(TRES);
        params.put("tipoDocumentoEmpleador", tipoDocumentoEmpleador.getEquivalencia());
        params.put("nroDocumentoEmpleador", nroDocumentoEmpleador);
        params.put("tipoDocumento", tipoDocumento);
        params.put("dniAfiliado", dniAfiliado);
        params.put("tipoCotizante", tipoCotizante);

        try {
            return getJdbcTemplate().queryForObject(sql, params, (ResultSet rs, int index) -> {
                RespuestaUltimoPeriodoCotizado rupc = new RespuestaUltimoPeriodoCotizado();
                rupc.setLey(rs.getString("CDLEY"));
                rupc.setPeriodo(rs.getString("NMPERIODO"));

                return rupc;
            });
        } catch (EmptyResultDataAccessException erdae) {
            LOG.debug(
                    "No encontró resultado para ultimos valores cotizados para dni:{}, tipoCot:{}, empleador:{}, tipoDocEmpleador:{} ",
                    dniAfiliado, tipoCotizante, nroDocumentoEmpleador, tipoDocumentoEmpleador.getEquivalencia());
            return null;
        }
    }

    /**
     * 
     * @param ley
     * @param dniAfiliado      numero de documento del afiliado
     * @param tipoCotizante
     * @param tipoDocEmpleador
     * @param nroDocEmpleador
     * @return
     */
    public RespuestaUltimosValoresCotizados obtenerUltimosValoresCotizados(String ley, String dniAfiliado,
            String tipoCotizante, TipoDocumento tipoDocEmpleador, String nroDocEmpleador, String periodo, String tipoDocumento) {
        String sql = getVarEntorno().getValor("consulta.ultimosValoresCotizados.2388");
        if (ley == null || LEY_1747.equals(ley)) {
            sql = getVarEntorno().getValor("consulta.ultimosValoresCotizados.1747");
        }

        Map<String, Object> params = new HashMap<String, Object>(5);
        params.put("dniAfiliado", dniAfiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoDocEmpleador", tipoDocEmpleador.getEquivalencia());
        params.put("nroDocEmpleador", nroDocEmpleador);
        params.put("tipoDocumento", tipoDocumento);
        params.put("periodo", periodo);

        try {
            return getJdbcTemplate().queryForObject(sql, params, (ResultSet rs, int index) -> {
                RespuestaUltimosValoresCotizados ruvc = new RespuestaUltimosValoresCotizados();
                ruvc.setIbc(rs.getLong("ULTIMO_IBC_COTIZADO"));
                ruvc.setSalario(rs.getLong("ULTIMO_SALARIO_COTIZADO"));
                ruvc.setDias(rs.getInt("DIAS_COTIZADOS"));
                ruvc.setAfp(rs.getString("AFP"));
                ruvc.setEps(rs.getString("EPS"));
                return ruvc;
            });
        } catch (DataAccessException e) {
            return null;
        }

    }

    public Optional<RespuestaUltimosValoresCotizados> obtenerUltimosValoresCotizadosNoLaborado(String ley,
            String dniAfiliado, String tipoCotizante, TipoDocumento tipoDocEmpleador, String nroDocEmpleador,
            String periodo, String tipoDocumento) {
        String sql = getVarEntorno().getValor("consulta.ultimosValoresCotizados.noLaborado");

        Map<String, Object> params = new HashMap<String, Object>(5);
        params.put("dniAfiliado", dniAfiliado);
        params.put("tipoDocumento", tipoDocumento);
        params.put("tipoCotizante", tipoCotizante);
        params.put("tipoDocEmpleador", tipoDocEmpleador.getEquivalencia());
        params.put("nroDocEmpleador", nroDocEmpleador);
        params.put("periodo", periodo);

        try {
            RespuestaUltimosValoresCotizados resultado = getJdbcTemplate().queryForObject(sql, params,
                    (ResultSet rs, int index) -> {
                        RespuestaUltimosValoresCotizados ruvc = new RespuestaUltimosValoresCotizados();
                        ruvc.setIbc(rs.getLong("ULTIMO_IBC_COTIZADO"));
                        ruvc.setSalario(rs.getLong("ULTIMO_SALARIO_COTIZADO"));
                        ruvc.setDias(rs.getInt("DIAS_COTIZADOS"));

                        return ruvc;
                    });
            return Optional.of(resultado);
        } catch (EmptyResultDataAccessException erdae) {
            LOG.debug(
                    "No encontró resultado para ultimos valores cotizados para dni:{}, tipoCot:{}, periodo:{}, empleador:{}, tipoDocEmpleador:{} ",
                    dniAfiliado, tipoCotizante, periodo, nroDocEmpleador, tipoDocEmpleador.getEquivalencia());
            return Optional.empty();
        }
    }

    public void actualizar(String dniAfiliado, String tipoCotizante, String poliza, Long ultimoIBC, Long ultimoSalario,
            String ultimoPeriodo, String eps, String afp) {
        String sql = getVarEntorno().getValor("actualizacion.ultimosValoresCotizados");

        Map<String, Object> params = new HashMap<String, Object>(9);
        params.put("dniAfiliado", dniAfiliado);
        params.put("tipoCotizante", tipoCotizante);
        params.put("poliza", poliza);
        params.put("ultimoIBC", ultimoIBC);
        params.put("ultimoSalario", ultimoSalario);
        params.put("ultimoPeriodo", ultimoPeriodo);
        params.put("dniModifica", getVarEntorno().getValor("usuario.dniingresa"));
        params.put("eps", eps);
        params.put("afp", afp);

        // TODO Se desactiva actualizacion para no modificar los valores de dllo en cpr
        // getJdbcTemplate().update(sql, params);
    }

    public class RespuestaUltimoPeriodoCotizado {
        private String periodo; // YYYYMM
        private String ley;

        public String getPeriodo() {
            return periodo;
        }

        public void setPeriodo(String periodo) {
            this.periodo = periodo;
        }

        public String getLey() {
            return ley;
        }

        public void setLey(String ley) {
            this.ley = ley;
        }

    }

    public class RespuestaUltimosValoresCotizados {
        private Long salario;
        private Long ibc;
        private Integer dias;
        private String eps;
        private String afp;

        public Long getSalario() {
            return salario != null ? salario : 0L;
        }

        public void setSalario(Long salario) {
            this.salario = salario;
        }

        public Long getIbc() {
            return ibc != null ? ibc : 0L;
        }

        public void setIbc(Long ibc) {
            this.ibc = ibc;
        }

        public Integer getDias() {
            return dias != null ? dias : 0;
        }

        public void setDias(Integer dias) {
            this.dias = dias;
        }

        public String getEps() {
            return eps;
        }

        public void setEps(String eps) {
            this.eps = eps;
        }

        public String getAfp() {
            return afp;
        }

        public void setAfp(String afp) {
            this.afp = afp;
        }

    }
}
