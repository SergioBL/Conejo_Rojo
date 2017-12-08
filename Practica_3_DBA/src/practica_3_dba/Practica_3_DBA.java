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
    public static void main(String[] args) {
        // TODO code application logic here
        SingleAgent vehiculo1, vehiculo2, vehiculo3, vehiculo4;
        SingleAgent pizarra;
        SingleAgent repostaje;
        AgentsConnection.connect("isg2.ugr.es",6000,"Achernar","Leon","Matute",false);
        System.out.println("\n");
        //reconocimiento = new Reconocimiento(new AgentID("reconocimiento15"));
        //repostaje = new Repostaje(new AgentID("repostaje15"));
        //repostaje.start(); 
        //reconocimiento.start();
        //vehiculo = new Vehiculo(new AgentID("vehiculo15"));
        //vehiculo.start();
    }
    
}