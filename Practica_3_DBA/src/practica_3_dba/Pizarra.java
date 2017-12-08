/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author salome
 */
public class Pizarra extends SingleAgent{
    
    private int bateria;
    private JSONObject envio;
    private JSONObject recepcion;
    private ACLMessage outbox, inbox;
    private int mapa_compartido[][];
    private int scanner_compartido[][];
    private Map<String, Tipo> vehiculos;
    private boolean finalizar;
    

    /**
    *
    * @author 
    */
    public Pizarra(AgentID aid) throws Exception {
        super(aid);
        outbox = null;
        inbox = null;  
        finalizar = false;
    }
    
    /**
    *
    * @author 
    */
    public void conexion(){
        
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
        if(!inbox.getContent().equals("\"CRASHED\"")){
            recepcion = new JSONObject(inbox.getContent());
            //recepcion_plano = recepcion.toString();
            //System.out.println("Pizarra: " + recepcion_plano);
        }else
            finalizar = true;
    }
    
    /**
    *
    * @author 
    */
    public void moverAgente(String nombreAgent, String objectivo){
        
    }
    
    /**
    *
    * @author 
    */
    @Override
    public void execute(){
        while(!finalizar){
            try {
                recibir_mensaje();
                actuar();
            } catch (InterruptedException | JSONException ex) {
                Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    @Override
    public void init(){
        System.out.println("Pizarra: vivo");
    }
    
    /**
    *
    * @author 
    */
    public void actuar() throws JSONException{
        
        if(recepcion.has("vehiculo")){
            if(recepcion.getString("vehiculo").equals("cerrar"))
                finalizar = true;
        }else if(recepcion.has("battery")){
            //Actuar según niveles de batería
            //recepcion_plano = recepcion.getString("battery");
            //bateria = (int) Float.parseFloat(recepcion_plano);
            envio = new JSONObject();
            if(bateria <= 2)//mandar mensaje de repostaje a vehiculo
                envio.put("pizarra","Pizarra");
            else//mandar mensaje ok a vehiculo
                envio.put("pizarra","OK");
            enviar_mensaje(envio.toString(), "vehiculo15");
        }
    }
    
}
