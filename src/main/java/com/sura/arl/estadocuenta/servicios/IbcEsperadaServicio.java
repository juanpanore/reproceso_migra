package com.sura.arl.estadocuenta.servicios;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sura.arl.afiliados.modelo.Afiliado;
import com.sura.arl.estadocuenta.accesodatos.IbcCotizacionDao;
import com.sura.arl.estadocuenta.accesodatos.IbcCotizacionDao.RespuestaIbcEsperado;
import com.sura.arl.estadocuenta.modelo.RespuestaIbc;
import com.sura.arl.reproceso.util.RedondeosUtil;


@Service
public class IbcEsperadaServicio {
    static final String PERIODO_REGLA = "201702";
    static final Integer N100 = 100;
    static final Integer N0 = 0;
    static final Integer N1 = 1;
    static final Integer N1000 = 1000;
    static final Integer N30 = 30;
    static final String LEY2388 = "2388";
    static final String S = "S";
    static final String N = "N";

    private IbcCotizacionDao ibcCotizacionDao;
    private static final Logger LOG = LoggerFactory.getLogger(IbcEsperadaServicio.class);

    @Autowired
    public IbcEsperadaServicio(IbcCotizacionDao ibcCotizacionDao) {
        super();
        this.ibcCotizacionDao = ibcCotizacionDao;
    }

    public RespuestaIbc calcularIbc(Afiliado afiliado, Integer diasEsperados) throws IbcNoEncontradoExcepcion {

        String msg = "";
        Double ibc = 0D;
        Double salarioMinimoPeriodo = ibcCotizacionDao
                .consultarSalarioMinimoXperiodo(afiliado.getCondicion().getPeriodoCotizacion());
        Double topeMinIBC = afiliado.getCondicion().getIbcMinimo() * salarioMinimoPeriodo;
        Double topeMaxIBC = afiliado.getCondicion().getIbcMaximo() * salarioMinimoPeriodo;
        Double salarioEncontrado = null;
        Optional<RespuestaIbcEsperado> valoresNovedades = Optional.empty();

        // ajuste 10092018
        // solo busca las novedades solo si el periodo es mayor q la fealta de
        // la
        // cobertura
        if (N.equals(afiliado.getCobertura().periodoEsMenorFealta())
                && N.equals(afiliado.getCobertura().esMismoPeriodoDeAlta())) {
            msg = msg.concat(" - periodo>fealta");

            // busca los valores del ibc de novedades del periodo anterior
            // valido
            valoresNovedades = Optional.ofNullable(ibcCotizacionDao.consultarIbcNovedades(afiliado));
        } else {
            msg = msg.concat(" - periodo<fealta, no se buscan nov.ant.");
        }

        // si encontro novedades anteriores validas
        if (valoresNovedades.isPresent()) {
            msg = msg.concat(" - Nov encontradas");

            RespuestaIbcEsperado novedades = valoresNovedades.get();

            // si es 2388
            if (LEY2388.equals(novedades.getLey())) {
                msg = msg.concat(" - es 2388");

                // regla 1
                if (novedades.getTotalNovedadesAusentismo() > 0 && novedades.getTotalNovedadesLaboradas() == 0
                        && novedades.getTotalDiasAusentismo() >= 30) {
                    ibc = novedades.getMaximoSalario();
                    msg = msg.concat(" -R1 ibc=sal max - max.sal:" + novedades.getMaximoSalario());
                }

                // regla 2
                if (novedades.getTotalNovedadesAusentismo() > 0 && novedades.getTotalNovedadesLaboradas() > 0) {
                    // ibc = obtenerSumatoriaIbc(novedades.getSumatoriaIbc(),
                    // novedades.getTotalDiasLaborados());
                    // msg = msg.concat(" -R2 ibc=sumatoria ibc");
                    ibc = novedades.getMaximoSalario();
                    msg = msg.concat(" -R2 ibc=sal max - max.sal:" + novedades.getMaximoSalario());
                }

                // regla 3
                if (novedades.getTotalNovedadesAusentismo() == 0 && novedades.getTotalNovedadesLaboradas() > 0
                        && novedades.getTotalDiasLaborados() < 30) {
                    ibc = novedades.getMaximoSalario();
                    msg = msg.concat(" -R3 ibc=sal max - max.sal:" + novedades.getMaximoSalario());

                }

                // regla 4
                if (novedades.getTotalNovedadesAusentismo() == 0 && novedades.getTotalNovedadesLaboradas() > 0
                        && novedades.getTotalDiasLaborados() >= 30) {
                    ibc = novedades.getTotalIbcLaborados();
                    msg = msg.concat(" -R4 ibc=sumatoria ibc laborados");
                }
            } else {
                msg = msg.concat(" - es 1747");

                // regla 1 - para la 1747 se toman los dias cotizados que viene en total dias
                if (novedades.getTotalDiasMaxIbc() > 0) {
                    ibc = obtenerIbcProporcional(novedades.getMaximoIbc(), novedades.getTotalDiasMaxIbc());
                    msg = msg.concat(" -R1 ibc=maximo ibc");
                } else {
                    // regla 2
                    ibc = novedades.getMaximoSalario();
                    msg = msg.concat(" -R2 ibc=sal max - max.sal " + novedades.getMaximoSalario());
                }
            }

            if (ibc == 0) {
                ibc = afiliado.getUltimoIbc();
                msg = msg.concat(" - ibc=ultimo ibc");
                // si el ultimo ibc = 0, entoncs se le asigna el salario
                if (ibc == 0) {
                    ibc = afiliado.getSalario().doubleValue();
                    msg = msg.concat(" - no encontro ultimoIbc, ibc=salario");
                    if (ibc == 0) {
                        ibc = salarioMinimoPeriodo;
                        msg = msg.concat(" - salario afiliado cero, ibc=salario minimo");
                    }
                }
            }

            // setea el salario encontrado en novedades
            salarioEncontrado = novedades.getMaximoSalario();
        } else {
            // ultimo ibc cotizado que aparece en cuerpoliza riesgo
            // ibc = afiliado.getUltimoIbc();
            msg = msg.concat(" - no se encontraron nov. ant.");

            // ajuste 240818
            if (S.equals(afiliado.getCobertura().esMismoPeriodoDeAlta())) {
                ibc = afiliado.getSalario().doubleValue();
                msg = msg.concat(" - ibc=salario");
            } else {
                ibc = afiliado.getUltimoIbc();
                msg = msg.concat(" - ibc=ultimo ibc");
                // si el ultimo ibc = 0, entoncs se le asigna el salario
                if (ibc == 0) {
                    ibc = afiliado.getSalario().doubleValue();
                    msg = msg.concat(" - no encontro ultimoIbc, ibc=salario");
                    if (ibc == 0) {
                        ibc = salarioMinimoPeriodo;
                        msg = msg.concat(" - salario afiliado cero, ibc=salario minimo");
                    }
                }
            }
        }

        if (ibc == null) {
            throw new IbcNoEncontradoExcepcion("Ibc no encontrado");
        }
        
        // se corrige el ibc segun topes
        // si el top min ibc es mayor al calculado, se corrige el calculado
        if (topeMinIBC.compareTo(ibc) > N0) {
            msg = msg.concat(" - " + ibc + "<" + topeMinIBC + " se asigna minimo");
            ibc = topeMinIBC;
        }
        
        // si el tope max ibc es menor al calculado, se corrige el calculado
        if (topeMaxIBC.compareTo(ibc) < N0) {
            msg = msg.concat(" - " + ibc + ">" + topeMaxIBC + " se asigna maximo");
            ibc = topeMaxIBC;
        }
       
        
        ibc = proporcionarIbcConRedondeo(ibc, diasEsperados, N30, afiliado.getCondicion().getPeriodoCotizacion());
       
        // Si luego de redondear, este nuevo valor superar los topes y los dias
        // son 30
        // (si es proporcional el valor va a dar menor al tope, por eso se
        // condiciona a 30)
        if (N30 == diasEsperados) {
            if (topeMaxIBC.compareTo(ibc) < N0) {
                msg = msg.concat(" - " + ibc + ">" + topeMaxIBC
                        + " ibc redondeado supera el tope maximo, asigna maximo proporcinal");
                ibc = topeMaxIBC;
            }

            if (topeMinIBC.compareTo(ibc) > N0) {
                msg = msg.concat(" - " + ibc + "<" + topeMinIBC
                        + " ibc redondeado es inferior al tope minimo, se asigna minimo proporcional");
                ibc = topeMinIBC;
            }
        }

        // si no se pudo encontrar salario, se toma el de cuerpoliza riesgo
        if (salarioEncontrado == null) {
            salarioEncontrado = Double.valueOf(afiliado.getSalario());
        }

        // si el tope min ibc es mayor al salario, se corrige el salario al top
        // min ibc
        if (topeMinIBC.compareTo(salarioEncontrado) > 0) {
            salarioEncontrado = topeMinIBC;
        }

        LOG.debug(
                "[Calculo ibc] dni:{}, periodo:{}, poliza:{}, tipoAfiliado:{} ---> dias:{}, ibc:{}, salario:{}, ultimoIbc:{}, tipoCotizante:{}, fealta:{}, febaja:{}, msg:{}",
                afiliado.getDni(), afiliado.getCondicion().getPeriodoCotizacion(), afiliado.getCobertura().getPoliza(),
                afiliado.getTipoAfiliado(), diasEsperados, ibc, salarioEncontrado, afiliado.getUltimoIbc(), afiliado.getTipoCotizante(),
                afiliado.getCobertura().getFealta(), afiliado.getCobertura().getFebaja(), msg);

        return new RespuestaIbc(ibc, Optional.of(salarioEncontrado), Optional.of(msg));
    }

    public static class IbcNoEncontradoExcepcion extends RuntimeException {

        private static final long serialVersionUID = 3121381383393533627L;

        public IbcNoEncontradoExcepcion() {
            super();
        }

        public IbcNoEncontradoExcepcion(String message) {
            super(message);
        }

    }

    private double proporcionarIbcConRedondeo(double ibc, int dias, int proporcionarCon, String periodo) {

        // el resultado de ibc, se hace proporcional a los dias
        double resultado = (ibc * dias) / proporcionarCon;
        // redondeo del ibc dependiendo decreto segun fecha
        return RedondeosUtil.redondearIbc(resultado, periodo);
}

    private double obtenerIbcProporcional(double ibc, int dias) {

        if (dias == N0) {
            return ibc;
        }
        return (ibc * N30) / Double.min(dias, N30);
    }
}
