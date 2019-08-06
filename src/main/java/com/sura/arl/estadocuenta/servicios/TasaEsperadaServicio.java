package com.sura.arl.estadocuenta.servicios;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.TasaEsperadaDao;
import com.sura.arl.estadocuenta.modelo.DatosTasa;



@Service
public class TasaEsperadaServicio {

    private TasaEsperadaDao tasaEsperadaDao;

    @Autowired
    public TasaEsperadaServicio(TasaEsperadaDao tasaEsperadaDao) {
        this.tasaEsperadaDao = tasaEsperadaDao;
    }

    public DatosTasa consultarTasa(Afiliado afiliado) throws TasaNoEncontradaExcepcion {
        try {
            return tasaEsperadaDao.consultarTasa(afiliado.getCobertura().getPoliza(),
                    afiliado.getCondicion().getPeriodoCotizacion(), afiliado.getDni(),
                    afiliado.getCondicion().getTipoTasa(), afiliado.getTipoCotizante(), 
                    afiliado.getTipoAfiliado());
        } catch (EmptyResultDataAccessException e) {
            throw new TasaNoEncontradaExcepcion("Tasa no encontrada");

        }

    }

    public CompletableFuture<DatosTasa> calcularTasaEsperada(Afiliado afiliado, Executor executor) {
        return CompletableFuture.supplyAsync(() -> consultarTasa(afiliado), executor);
    }

    public class TasaNoEncontradaExcepcion extends RuntimeException {

        private static final long serialVersionUID = 3121381383393533627L;

        public TasaNoEncontradaExcepcion(String message) {
            super(message);
        }
    }

}
