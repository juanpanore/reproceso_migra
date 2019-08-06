package com.sura.arl.reproceso.servicios;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.reproceso.accesodatos.NovedadesDao;
import com.sura.arl.reproceso.accesodatos.RenesDao;

@Service
public class RenesServicio {

    private final RenesDao renesDao;
    private final NovedadesDao novedadesDao;

    @Autowired
    public RenesServicio(RenesDao renesDao, NovedadesDao novedadesDao) {
        super();
        this.renesDao = renesDao;
        this.novedadesDao = novedadesDao;
    }

    /**
     * Valida si existen renes cruzando los registros de los afiliados registrados
     * en la planilla en la legalizacion (novedades), junto con los afiliados
     * esperados, si existen en novedades pero no esperadas se asume como un RENE,
     * por lo tanto se registra.
     * 
     * @param numeroFormulario
     * @param dniAfiliado
     * @param tipoCotizante
     * @param tipoAfiliado
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer registrar(Long numeroFormulario, String dniAfiliado, String tipoCotizante, String tipoAfiliado) {

        return registrarRene(numeroFormulario, dniAfiliado, tipoCotizante, tipoAfiliado);
    }

    public Integer registrarRene(Long numeroFormulario, String dniAfiliado, String tipoCotizante, String tipoAfiliado) {

        String errmsg = " no puede ser nulo, afiliado: " + dniAfiliado + ", tipoCot:" + tipoCotizante
                + ", numeroFormulario:" + numeroFormulario;

        Objects.requireNonNull(numeroFormulario, "El numero de formulario" + errmsg);
        Objects.requireNonNull(dniAfiliado, "El DNI del afiliado" + errmsg);
        //Objects.requireNonNull(tipoAfiliado, "El tipo de afiliado" + errmsg);
        // Objects.requireNonNull(tipoCotizante, "El tipo de cotizante"+errmsg);

        // Busca reportados no esperados y los registra
        int resultado = renesDao.registrar(numeroFormulario, dniAfiliado, tipoCotizante, tipoAfiliado);

        //if (resultado > 0) {
            String dni = dniAfiliado.replaceAll("[^0-9.]", "");
            novedadesDao.actualizarErrorRenesNovedad(dni, numeroFormulario, true, dni.substring(0,1));
        //}
        return resultado;
    }

    @Transactional
    public Integer borrar(String poliza, String dniAfiliado, String periodo, String tipoAfiliado, Long formulario) {
        int resultado = 0;
        if (formulario == null) {
            resultado = renesDao.borrar(poliza, dniAfiliado, periodo, tipoAfiliado);
        } else {
            resultado = renesDao.borrar(poliza, dniAfiliado, periodo, tipoAfiliado, formulario);

            //if (resultado > 0) {
                String dni = dniAfiliado.replaceAll("[^0-9.]", "");
                novedadesDao.actualizarErrorRenesNovedad(dni, formulario, false, dni.substring(0,1));
            //}
        }
        return resultado;
    }
    
    @Transactional
    public Integer borrarSinCobertura(String poliza, String dniAfiliado, String periodo, String formulario){
        return renesDao.borrarSinCobertura(poliza, dniAfiliado, periodo, formulario);
    }

    public Boolean existe(String poliza, String dniAfiliado, String periodo, String tipoAfiliado, Long formulario) {
        return renesDao.existe(poliza, dniAfiliado, periodo, tipoAfiliado, formulario);
    }

}
