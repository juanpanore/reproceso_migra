package com.sura.arl.reproceso.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

import com.sura.arl.reproceso.modelo.excepciones.DatoIncorrectoExcepcion;

@Component
public class ValidacionEmail {

    private final static String REGLA_CORREO_VALIDO = "^(?![0-9]+)[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]{2,}(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final static String REGLA_CORREO_VALIDO_GMAIL = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@gmail.com$";
    private final static String REGLA_LETRAS_REPETIDAS = "^([a-zA-Z])\\1*(\\.([a-zA-Z])\\1*)*@.+$";
    private final static String REGLA_ID_INVALIDO_EXACTO = "^(pila|ab|no|na|n.o|noaplica)+(\\.(pila|ab|no|na|n.o|noaplica)*)*@.*$";
    private final static String REGLA_ID_INVALIDO_CONTIENE = "noexisto|noexiste|notengocorreo|sincorreo|notiene|notengo|notinee|pailas"
                                                  + "|sinconfirmar|notine|notiewne|no.tiene|@lo.com";
    private final static String REGLA_CORREO_VALIDO_FELIMITE_PAGO= "^[A-Z0-9\\._%+-]{2,}@[A-Z0-9.-]{2,}\\.[A-Z]{2,6}$";
    private final static String REGLA_LETRAS_REPETIDAS_FELIMITE_PAGO = "^([a-zA-Z])\\1+(\\.([a-zA-Z])\\1+)*@.+$";

    
    public ValidacionEmail() {
        super();
    }

    public ValidacionEmail(String email) {
        if (!esEmailValido(email)) {
            throw new DatoIncorrectoExcepcion("Email invalido:" + email);
        }
        
        if(reglaValidacionCoincidente(email, REGLA_CORREO_VALIDO_GMAIL) || reglaValidacionCoincidente(email, REGLA_CORREO_VALIDO)){
            if (reglaValidacionCoincidente(email, REGLA_LETRAS_REPETIDAS)) {
                throw new DatoIncorrectoExcepcion("Email invalido, regla letras repetidas:" + email);
            }
    
            if (reglaValidacionCoincidente(email, REGLA_ID_INVALIDO_EXACTO)) {
                throw new DatoIncorrectoExcepcion("Email invalido, regla id exacto invalido:" + email);
            }
    
            if (reglaValidacionCoincidente(email, REGLA_ID_INVALIDO_CONTIENE)) {
                throw new DatoIncorrectoExcepcion("Email invalido, regla id contiene invalido:" + email);
            }
        } else{
            throw new DatoIncorrectoExcepcion("Email invalido, regla correo valido:" + email);
        }
    }

    public boolean esEmailValido(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public boolean validarEmail(String email) {

        return  reglaValidacionCoincidente(email, REGLA_CORREO_VALIDO_FELIMITE_PAGO) && !reglaValidacionCoincidente(email, REGLA_LETRAS_REPETIDAS_FELIMITE_PAGO) && !reglaValidacionCoincidente(email, REGLA_ID_INVALIDO_EXACTO)
                && !reglaValidacionCoincidente(email, REGLA_ID_INVALIDO_CONTIENE);
    }

    public boolean reglaValidacionCoincidente(String email, String regla) {
        Pattern p = Pattern.compile(regla, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email.toLowerCase());
        return m.find();
    }
}
