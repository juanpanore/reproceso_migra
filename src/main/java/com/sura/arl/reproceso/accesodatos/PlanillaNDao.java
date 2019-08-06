package com.sura.arl.reproceso.accesodatos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;

import com.sura.arl.general.accesodatos.AbstractDAO;
import com.sura.arl.reproceso.modelo.excepciones.AccesoDatosExcepcion;

@Repository
public class PlanillaNDao extends AbstractDAO {

    private static final Logger LOG = LoggerFactory.getLogger(PlanillaNDao.class);

    static final String S = "S";
    static final String N = "N";

    @Autowired
    public PlanillaNDao(PlatformTransactionManager transactionManager) {
    }

    public Optional<DatosPlanillaN> obtenerDatosPadre(Long numeroFormulario) {

        if (numeroFormulario == null) {
            throw new AccesoDatosExcepcion("No es posible buscar sin datos");
        }
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("numeroFormulario", numeroFormulario);

        try {
            DatosPlanillaN resultado = getJdbcTemplate().queryForObject("consultar.datos.planillaN", params,

                    new RowMapper<DatosPlanillaN>() {
                        public DatosPlanillaN mapRow(ResultSet rs, int rowNum) throws SQLException {
                            DatosPlanillaN datos = new DatosPlanillaN();

                            datos.setNumeroFormularioPadre(rs.getLong("FORMULARIO_PADRE"));
                            datos.setTipoPlanillaN(rs.getString("TIPO_N"));

                            return datos;
                        }
                    });
            return Optional.of(resultado);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    public static class DatosPlanillaN {
        private Long numeroFormularioPadre;
        private String tipoPlanillaN;

        public Long getNumeroFormularioPadre() {
            return numeroFormularioPadre;
        }

        public void setNumeroFormularioPadre(Long numeroFormularioPadre) {
            this.numeroFormularioPadre = numeroFormularioPadre;
        }

        public String getTipoPlanillaN() {
            return tipoPlanillaN;
        }

        public void setTipoPlanillaN(String tipoPlanillaN) {
            this.tipoPlanillaN = tipoPlanillaN;
        }

    }
}
