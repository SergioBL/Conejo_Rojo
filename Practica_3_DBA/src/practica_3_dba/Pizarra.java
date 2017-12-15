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
    private String conversacion_id;
    private boolean objetivoEncontrado;
    private boolean EnObjetivo;//asociarlo a vehiculo
    

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
    public void conexion() throws JSONException, InterruptedException{
        envio = new JSONObject();
        envio.put("world","map1");
        enviar_mensaje(envio.toString(), "Achernar", ACLMessage.SUBSCRIBE);
        recibir_mensaje();
        envio = new JSONObject();
        envio.put("ID",conversacion_id);
        for(int i = 1; i <= 4; i++)
            enviar_mensaje(envio.toString(), "vehiculo"+i, ACLMessage.REQUEST);
    }
    
    /**
    *
    * @author 
    */
    public void enviar_mensaje(String mensaje, String receptor, int performativa){
        System.out.println("Envia: " +mensaje+receptor);
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(mensaje);
        outbox.setPerformative(performativa);
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
        if(inbox.getPerformativeInt()==ACLMessage.INFORM){
            System.out.println("Conversacion: " + inbox.getConversationId());
            conversacion_id = inbox.getConversationId();
        }else if(inbox.getPerformativeInt() == ACLMessage.NOT_UNDERSTOOD){
            System.out.println("Pizarra NOTUNDERSTOOD: " + recepcion_plano);
        }else if(inbox.getPerformativeInt() == ACLMessage.FAILURE){
            System.out.println("Pizarra FAILURE: " + recepcion_plano);
        }else if(inbox.getPerformativeInt() == ACLMessage.REFUSE){
            System.out.println("Pizarra REFUSE: " + recepcion_plano);
        }
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
        try {
            conexion();
            while(!finalizar){
            try {
            recibir_mensaje();
            actuar();
            } catch (InterruptedException | JSONException ex) {
            Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        } catch (JSONException | InterruptedException ex) {
            Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
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
        
        ////////////////////////Busqueda////////////////////////
        
        if(!objetivoEncontrado){
            if(recepcion.has("TipoVehiculo")){
                 String tipoVehiculo = recepcion.getString("TipoVehiculo");
                //nombre del vehiculo y otra info
                
             }else if(recepcion.has("MapaAux")){
                 //sacar todo
                  objetivoEncontrado = recepcion.getBoolean("Objetivo");
                  //mas info Mapaaux , Scaner , etcc
            }
        }
        
        /////////////////////Fin-Busqueda///////////////////////////
        
        /////////////////////Llegada-Obretivo///////////////////////
        
        if(objetivoEncontrado && !EnObjetivo){
            if(recepcion.has("EnObjetivo")){
                 EnObjetivo = recepcion.getBoolean("EnObjetivo");
                 
                //nombre del vehiculo
             }else if(recepcion.has("")){
            }
            
        }
        ////////////////////Fin-llegadaObjetivo////////////////////////////
        
        ///////////////////////Baterias agotadas y refuel Agotado///////////////////
        
        if(recepcion.has("SinRefuel")){
                 boolean sinRefuel = recepcion.getBoolean("SinRefuel"); 
         //enviar CLOSE al server y recoger traza
         finalizar=true;
        }
        ///////////////////////Fin-Baterias agotadas y refuel Agotado///////////////////
        
    }
    
}
