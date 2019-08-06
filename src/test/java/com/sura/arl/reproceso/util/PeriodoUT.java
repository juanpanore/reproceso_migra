package com.sura.arl.reproceso.util;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author pragma.co
 */
@RunWith(MockitoJUnitRunner.class)
public class PeriodoUT {
    
    @Test
    public void getPeridosTest(){
        
        Periodo inicio = Periodo.of(2018, 1);
        Periodo fin = Periodo.of(2018, 5);
        
        List<Periodo> list = Periodo.getPeridos(inicio, fin);
        
        Assert.assertTrue("Primer periodo mal generado",list.get(0).isEqual(inicio));
        Assert.assertTrue("Ultimo periodo mal generado",list.get(list.size()-1).isEqual(fin)); 
        Assert.assertTrue("Error generando periodos!",list.size() == 5);
    }
    
    @Test
    public void getPeridos1Test(){
        
        Periodo inicio = Periodo.of(2018, 1);
        Periodo fin = Periodo.of(2018, 1);
        
        List<Periodo> list = Periodo.getPeridos(inicio, fin);
        
        Assert.assertTrue("Primer periodo mal generado",list.get(0).isEqual(inicio));
        Assert.assertTrue("Ultimo periodo mal generado",list.get(list.size()-1).isEqual(fin)); 
        Assert.assertTrue("Error generando periodos!",list.size() == 1);
    }
    
}
