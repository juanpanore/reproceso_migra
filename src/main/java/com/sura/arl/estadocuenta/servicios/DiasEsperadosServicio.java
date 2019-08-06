package com.sura.arl.estadocuenta.servicios;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.DiasEsperadosDao;
import com.sura.arl.estadocuenta.servicios.TasaEsperadaServicio.TasaNoEncontradaExcepcion;

@Service
public class DiasEsperadosServicio {

    private DiasEsperadosDao diasEsperadosDao;
    private ErroresProcesoServicio erroresProcesoServicio;

    static final Integer N01 = 1;

    @Autowired
    public DiasEsperadosServicio(DiasEsperadosDao diasEsperadosDao, ErroresProcesoServicio erroresProcesoServicio) {
        this.diasEsperadosDao = diasEsperadosDao;
        this.erroresProcesoServicio = erroresProcesoServicio;
    }

    public Integer consultarDiasEsperados(Afiliado afiliado) throws TasaNoEncontradaExcepcion {
        try {
            return diasEsperadosDao.consultarDiasEsperados(afiliado.getCobertura().getPoliza(), afiliado.getDni(),
                    afiliado.getCondicion().getPeriodoCotizacion(), afiliado.getCondicion().getIndicadorDias(),
                    afiliado.getCondicion().getTipoNovedad(), afiliado.getTipoCotizante(), afiliado.getTipoAfiliado());
        } catch (EmptyResultDataAccessException e) {
            throw new DiasNoEncontradoExcepcion("Dias esperados no encontrado");
        }
    }

    public Integer consultarMultipleCobertura(Afiliado afiliado) throws TasaNoEncontradaExcepcion {
        try {
            return diasEsperadosDao.consultarMultipleCobertura(afiliado.getCobertura().getPoliza(),
                    afiliado.getDni(), afiliado.getCondicion().getPeriodoCotizacion(),
                    afiliado.getTipoCotizante(), afiliado.getTipoAfiliado());
        } catch (EmptyResultDataAccessException e) {
            throw new DiasNoEncontradoExcepcion("Multiple cobertura no encontrada");
        }
    }

    public CompletableFuture<Integer> calcularDiasEsperados(Afiliado afiliado, Executor executor) {
        Integer diasEsperados = consultarDiasEsperados(afiliado);
        
        //Esta parte se comenta y se mueve para el metodo especifico de los afiliados con multiples coberturas.
        /*Integer multipleCobertura = consultarMultipleCobertura(afiliado);

        // control por multiples coberturas
        if (multipleCobertura > N01) {
            reportarTrazaMultiplesCoberturas(afiliado);
        }*/

        return CompletableFuture.supplyAsync(() -> diasEsperados, executor);
    }

    public static class DiasNoEncontradoExcepcion extends RuntimeException {

        private static final long serialVersionUID = 3121381383393533627L;

        public DiasNoEncontradoExcepcion() {
            super();
        }

        public DiasNoEncontradoExcepcion(String message) {
            super(message);
        }

    }

    /*public void reportarTrazaMultiplesCoberturas(Afiliado afiliado) {
        ErrorProceso error = ErrorProceso.builder().dni(afiliado.getDni())
                .periodo(afiliado.getCondicion().getPeriodoProceso())
                .periodoGeneracion(afiliado.getCobertura().getPeriodoGeneracion())
                .npoliza(afiliado.getCobertura().getPoliza())
                .tipoGeneracion(afiliado.getCondicion().getTipoGeneracion())
                .codError(CatalogoErrores.MULTIPLES_COBERTURAS)
                .tipoCotizante(afiliado.getTipoCotizante())
                .estadoError(EstadoError.POR_CORREGIR).build();

        erroresProcesoServicio.registrarErrorProceso(error);
    }*/

}
