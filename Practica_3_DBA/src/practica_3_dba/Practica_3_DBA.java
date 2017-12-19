/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 *
 * @author Sergio
 */


public class Practica_3_DBA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        SingleAgent vehiculo1, vehiculo2, vehiculo3, vehiculo4;
        SingleAgent pizarra;
        SingleAgent repostaje;
        AgentsConnection.connect("isg2.ugr.es",6000,"Achernar","Leon","Matute",false);
        System.out.println("\n");
        pizarra = new Pizarra(new AgentID("pizarra1"));
        vehiculo1 = new Vehiculo(new AgentID("vehiculo5"));
        vehiculo2 = new Vehiculo(new AgentID("vehiculo6"));
        vehiculo3 = new Vehiculo(new AgentID("vehiculo7"));
        vehiculo4 = new Vehiculo(new AgentID("vehiculo8"));
        vehiculo1.start();
        vehiculo2.start();
        vehiculo3.start();
        vehiculo4.start();
        pizarra.start();
    }
    
}