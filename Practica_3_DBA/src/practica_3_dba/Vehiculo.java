/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import org.codehaus.jettison.json.JSONObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author salome
 */
public class Vehiculo extends SingleAgent{
    
    private int fuelrate;
    private boolean fly;
    private int range;
    private int mapa[][];
    private int radar[][];
    private int gps_x;
    private int gps_y;
    private ACLMessage outbox, inbox; 
    private JSONObject envio;
    private JSONObject recepcion;
    private String key;
    private Boolean repostaje;
    private Boolean finalizar;
    private Boolean BUSCANDO;
    private Boolean LLEGANDO;
    private Boolean ESPERANDO;
    private Tipo tipo;
    private String conversacion_id;

    /**
    *
    * @author 
    */
    public Vehiculo(AgentID aid) throws Exception {
        super(aid);
        outbox = null;
        inbox = null;  
        finalizar = false;
    }
    
    /**
    *
    * @author 
    */
    public void conexion() throws InterruptedException, JSONException{
        recibir_mensaje();
    }
    
    /**
    *
    * @author 
    */
    public void enviar_mensaje(String mensaje, String receptor){
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(mensaje);
        this.send(outbox);
    }
    
    /**
    *
    * @author 
    */
    public void recibir_mensaje() throws InterruptedException, JSONException{
        inbox = receiveACLMessage();
        recepcion = new JSONObject(inbox.getContent());
        String recepcion_plano = recepcion.toString();
        System.out.println("Pizarra: " + recepcion_plano);
        if(inbox.getPerformativeInt()==ACLMessage.REQUEST){
            if(recepcion.has("ID")){
                conversacion_id = recepcion.getString("ID");
            }
        }
    }
    
    /**
    *
    * @author 
    */
    @Override
    public void execute(){
        try {
            conexion();
            /*while(!finalizar){
            try {
            recibir_mensaje();
            actuar();
            } catch (InterruptedException | JSONException ex) {
            Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
            }
            }*/
        } catch (InterruptedException ex) {
            Logger.getLogger(Vehiculo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(Vehiculo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    *
    * @author 
    */
    @Override
    public void finalize(){
        try {
            System.out.println("Pizarra muerto");
        } finally {
            super.finalize();
        }
    }

    /**
    *
    * @author 
    */
    public void actuar() throws JSONException{
        
        if(recepcion.has("vehiculo")){
            if(recepcion.getString("vehiculo").equals("cerrar"))
                finalizar = true;
        }else if(recepcion.has("fuelrate")){
            envio = new JSONObject();
            if(fuelrate <= 2)
                envio.put("pizarra","Pizarra");
            else//mandar mensaje ok a vehiculo
                envio.put("pizarra","OK");
            enviar_mensaje(envio.toString(), "vehiculo15");
        }
    }

}
