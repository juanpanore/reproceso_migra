package com.sura.arl.estadocuenta.accesodatos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.util.UtilCache;

@Repository
public class IbcCotizacionDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(IbcCotizacionDao.class);
    static final String ID_CACHE_SALMIN = "SAL_MIN_";

    public RespuestaIbcEsperado consultarIbcNovedades(Afiliado afiliado) {

		Map<String, Object> params = new HashMap<String, Object>();
        params.put("dni", afiliado.getDni().replaceAll("[^0-9.]", ""));
        params.put("tipoDocumento", String.valueOf(afiliado.getDni().charAt(0)));
        params.put("periodo", afiliado.getCondicion().getPeriodoCotizacion());
        params.put("npoliza", afiliado.getCobertura().getPoliza());
        params.put("tipoAfiliado", afiliado.getTipoAfiliado());
        params.put("tipoCotizante", afiliado.getTipoCotizante());

        try {
            return getJdbcTemplate().queryForObject(getVarEntorno().getValor("consulta.datos.ibc.novedades"), params,

                    (ResultSet rs, int rowNum) -> {
                        RespuestaIbcEsperado respuesta = new RespuestaIbcEsperado();

                        respuesta.setLey(rs.getString("LEY"));
                        respuesta.setMaximoSalario(rs.getDouble("MAXIMOSALARIO"));
						respuesta.setMaximoIbc(rs.getDouble("MAXIMOIBC"));
						respuesta.setTotalDias(rs.getInt("TOTALDIAS"));
						respuesta.setTotalDiasMaxIbc(rs.getInt("DIAS_MAX_IBC"));
						respuesta.setTotalDiasAusentismo(rs.getInt("TOTALDIASAUSENTISMO"));
						respuesta.setTotalDiasLabodos(rs.getInt("TOTALDIASLABODOS"));
                        respuesta.setTotalIbcLaborados(rs.getDouble("TOTALIBCLABORADOS"));
                        respuesta.setTotalNovedadesAusentismo(rs.getDouble("TOTALNOVEDADESAUSENTISMO"));
                        respuesta.setTotalNovedadesLaboradas(rs.getDouble("TOTALNOVEDADESLABORADAS"));

                        return respuesta;
                    });

        } catch (EmptyResultDataAccessException e) {
            // es posible que no haya datos, p ej: afiliacion sin novedades
            // creadas
            return null;
        }
    }

    public Double consultarSalarioMinimoXperiodo(String periodo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodo", periodo);

        // verifica en la cache si ya se encuentra el valor para devolver y no
        // hacer llamado a la bd
		Object salarioEnCache = UtilCache.obtener(ID_CACHE_SALMIN + periodo);
		if (salarioEnCache != null) {
			return new Double(salarioEnCache.toString());
		}

        try {
            Double salarioMinimo = getJdbcTemplate().queryForObject(
                    getVarEntorno().getValor("consulta.salarioMinimo.vigente.periodo"), params, Double.class);
            // guarda el valor en la cache para ser reutilizado

			UtilCache.agregar(ID_CACHE_SALMIN + periodo, salarioMinimo);
            return salarioMinimo;
        } catch (IncorrectResultSizeDataAccessException e) {
            LOG.error("Error al consultar el Salario Minimo por periodo {}", e.getMessage());
            throw new RuntimeException("Error calculando el Salario Minimo por periodo");
        }
    }

    public static class RespuestaIbcEsperado {

        private Double maximoIbc;
        private Integer totalDias;
        private Integer totalDiasMaxIbc;
        private Double maximoSalario;
        private Double totalNovedadesAusentismo;
        private Double totalNovedadesLaboradas;
        private Integer totalDiasAusentismo;
        private Integer totalDiasLaborados;
        private Double totalIbcLaborados;
        private String ley;

        public Double getMaximoIbc() {
            return maximoIbc;
        }

        public void setMaximoIbc(Double maximoIbc) {
            this.maximoIbc = maximoIbc;
        }

        public Integer getTotalDias() {
            return totalDias;
        }

        public void setTotalDias(Integer totalDias) {
            this.totalDias = totalDias;
        }

        public Double getMaximoSalario() {
            return maximoSalario;
        }

        public void setMaximoSalario(Double maximoSalario) {
            this.maximoSalario = maximoSalario;
        }

        public Double getTotalNovedadesAusentismo() {
            return totalNovedadesAusentismo;
        }

        public void setTotalNovedadesAusentismo(Double totalNovedadesAusentismo) {
            this.totalNovedadesAusentismo = totalNovedadesAusentismo;
        }

        public Double getTotalNovedadesLaboradas() {
            return totalNovedadesLaboradas;
        }

        public void setTotalNovedadesLaboradas(Double totalNovedadesLaboradas) {
            this.totalNovedadesLaboradas = totalNovedadesLaboradas;
        }

        public Integer getTotalDiasAusentismo() {
            return totalDiasAusentismo;
        }

        public void setTotalDiasAusentismo(Integer totalDiasAusentismo) {
            this.totalDiasAusentismo = totalDiasAusentismo;
        }

        public Integer getTotalDiasLaborados() {
            return totalDiasLaborados;
        }

        public void setTotalDiasLabodos(Integer totalDiasLaborados) {
            this.totalDiasLaborados = totalDiasLaborados;
        }

        public Double getTotalIbcLaborados() {
            return totalIbcLaborados;
        }

        public void setTotalIbcLaborados(Double totalIbcLaborados) {
            this.totalIbcLaborados = totalIbcLaborados;
        }

        public String getLey() {
            return ley;
        }

        public void setLey(String ley) {
            this.ley = ley;
        }
        
        public Integer getTotalDiasMaxIbc() {
			return totalDiasMaxIbc;
		}

		public void setTotalDiasMaxIbc(Integer totalDiasMaxIbc) {
			this.totalDiasMaxIbc = totalDiasMaxIbc;
		}

        @Override
        public String toString() {
            return "RespuestaIbcEsperado [maximoIbc=" + maximoIbc + ", totalDias=" + totalDias
                    + ", maximoSalario=" + maximoSalario + ", totalNovedadesAusentismo=" + totalNovedadesAusentismo
                    + ", totalNovedadesLaboradas=" + totalNovedadesLaboradas + ", totalDiasAusentismo="
                    + totalDiasAusentismo + ", totalDiasLaborados=" + totalDiasLaborados + ", totalIbcLaborados="
                    + totalIbcLaborados + ", ley=" + ley + ", totalDiasMaxIbc= " +  totalDiasMaxIbc + "]";
        }
        
        
    }

    public static class RespuestaRedondeoIbcCotizacion {

        private Double ibc;
        private Double cotizacion;

        public Double getIbc() {
            return ibc;
        }

        public void setIbc(Double ibc) {
            this.ibc = ibc;
        }

        public Double getCotizacion() {
            return cotizacion;
        }

        public void setCotizacion(Double cotizacion) {
            this.cotizacion = cotizacion;
        }

        public RespuestaRedondeoIbcCotizacion(Double ibc, Double cotizacion) {
            super();
            this.ibc = ibc;
            this.cotizacion = cotizacion;
        }

    }

}
