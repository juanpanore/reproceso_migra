package com.sura.arl.integrador.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.integrador.accesodatos.CambiosEstadoCuentaDao;
import com.sura.arl.integrador.exceptions.IntegradorEsperadasExcepcion;
import com.sura.arl.integrador.modelo.Registro;

@Service
public class CambiosEstadoCuentaServicio {

    private CambiosEstadoCuentaDao dao;
    
    private static final String C_SEPARATOR = "-";

    @Autowired
    public CambiosEstadoCuentaServicio(CambiosEstadoCuentaDao dao) {
        super();
        this.dao = dao;
    }
    
    public void escogerRegistrosAGestionar() {
        dao.escogerRegistrosAGestionar();
    }

    public List<Registro> consultarCambiosEstadoCuenta(Double inicio, Double fin) throws IntegradorEsperadasExcepcion {
        return dao.consultarCambiosEstadoCuenta(inicio, fin);
    }

    public String generarMensajeMQ(Registro registro) {

        StringBuilder llave = new StringBuilder();

        llave.append(registro.getDsParametros());

        return llave.toString();
    }

    public void actualizarRegistroATramitar(Registro registro) throws IntegradorEsperadasExcepcion{
        dao.actualizarRegistroATramitar(registro);
    }
    
    public void actualizarRegistrosATramitar(List<Registro> registros) throws IntegradorEsperadasExcepcion{
        dao.actualizarRegistrosATramitar(registros);
    }
    
    public List<Registro> consultarCambiosEstadoCuenta() throws IntegradorEsperadasExcepcion {
        return dao.consultarCambiosEstadoCuenta();
    }

}
