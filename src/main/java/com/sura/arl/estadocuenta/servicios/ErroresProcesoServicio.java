package com.sura.arl.estadocuenta.servicios;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sura.arl.estadocuenta.accesodatos.ErrorProcesoDao;
import com.sura.arl.estadocuenta.modelo.ErrorProceso;
import com.sura.arl.estadocuenta.modelo.ErrorProceso.EstadoError;
import com.sura.arl.reproceso.modelo.TipoGeneracion;
import com.sura.arl.reproceso.modelo.excepciones.ServicioExcepcion;
import com.sura.arl.reproceso.util.VariablesEntorno;
import java.util.Calendar;

@Service
public class ErroresProcesoServicio {

    @Autowired
    private ErrorProcesoDao errorProcesoDao;

    @Autowired
    private VariablesEntorno varEntorno;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(ErrorProceso entrada){
        registrarErrorProceso(entrada);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registrarErrorProceso(ErrorProceso entrada) {

        if (entrada == null) {
            throw new ServicioExcepcion("No es posible registrar un error de proceso sin datos");
        }

        if (entrada.getCodError() == null || entrada.getCodError().isEmpty()) {
            throw new ServicioExcepcion("Para registrar un error de proceso se necesita un codigo de error");
        }

		entrada.setUsuarioRegistro(entrada.getUsuarioRegistro() == null
				? getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA) : entrada.getUsuarioRegistro());
		entrada.setCodigoProceso(entrada.getCodigoProceso() == null
				? getVarEntorno().getValor(VariablesEntorno.ID_PROCESO_REPROCESO) : entrada.getCodigoProceso());

        getErrorProcesoDao().registrar(entrada);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(List<ErrorProceso> listaRegistros) {

        listaRegistros.forEach(registro -> {
            registro.setUsuarioRegistro(
                    registro.getUsuarioRegistro() == null ? getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA)
                            : registro.getUsuarioRegistro());
            registro.setCodigoProceso(registro.getCodigoProceso() == null
                    ? getVarEntorno().getValor(VariablesEntorno.ID_PROCESO_REPROCESO)
                    : registro.getCodigoProceso());
        });

        getErrorProcesoDao().registrarComoLote(listaRegistros);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErrorProcesoUnico(ErrorProceso entrada) {

        if (entrada == null) {
            throw new ServicioExcepcion("No es posible registrar un error de proceso sin datos");
        }

        if (entrada.getCodError() == null || entrada.getCodError().isEmpty()) {
            throw new ServicioExcepcion("Para registrar un error de proceso se necesita un codigo de error");
        }

		entrada.setUsuarioRegistro(entrada.getUsuarioRegistro() == null
				? getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA) : entrada.getUsuarioRegistro());
		entrada.setCodigoProceso(entrada.getCodigoProceso() == null
				? getVarEntorno().getValor(VariablesEntorno.ID_PROCESO_REPROCESO) : entrada.getCodigoProceso());

        getErrorProcesoDao().registrarUnico(entrada);
    }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registrarErroresProceso(List<ErrorProceso> listaRegistros) {

		listaRegistros.stream().forEach(registro -> {
			registro.setUsuarioRegistro(registro.getUsuarioRegistro() == null
					? getVarEntorno().getValor(VariablesEntorno.DNI_INGRESA) : registro.getUsuarioRegistro());
			registro.setCodigoProceso(registro.getCodigoProceso() == null
					? getVarEntorno().getValor(VariablesEntorno.ID_PROCESO_REPROCESO) : registro.getCodigoProceso());
		});

		getErrorProcesoDao().registrarComoLote(listaRegistros);
    }

	public static ErrorProceso construir(String poliza, String periodoGeneracion, String periodo, String codError,
			String descripcion, TipoGeneracion tipoGeneracion, String idProceso, String usuarioRegistro) {

		ErrorProceso errorProceso = ErrorProceso.builder().codError(codError).codigoProceso(idProceso)
				.usuarioRegistro(usuarioRegistro).tipoGeneracion(tipoGeneracion.getEquivalencia())
				.periodoGeneracion(periodoGeneracion).fechaRegistro(Calendar.getInstance().getTime()).npoliza(poliza)
				.periodo(periodo).observacion(descripcion).estadoError(EstadoError.POR_CORREGIR).build();

		return errorProceso;
	}

	@Transactional
	public void borrarErrores(String poliza, String periodoCotizacion) {
		errorProcesoDao.borrar(poliza, periodoCotizacion);
	}

    public ErrorProcesoDao getErrorProcesoDao() {
        return errorProcesoDao;
    }

    public void setErrorProcesoDao(ErrorProcesoDao errorProcesoDao) {
        this.errorProcesoDao = errorProcesoDao;
    }

    public VariablesEntorno getVarEntorno() {
        return varEntorno;
    }

    public void setVarEntorno(VariablesEntorno varEntorno) {
        this.varEntorno = varEntorno;
    }

}
