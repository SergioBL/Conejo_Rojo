/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author joaquin
 */
public class Pizarra extends SingleAgent{
    
    private int bateria;
    private JSONObject envio;
    private JSONObject recepcion;
    private ACLMessage outbox, inbox;
    private int mapa_compartido[][];
    private int scanner_compartido[][];
    private Map<String, Datosvehiculo> vehiculos;
    private boolean finalizar;
    private String conversacion_id;
    private boolean objetivoEncontrado;
    private boolean EnObjetivo;//asociarlo a vehiculo
    private JSONArray mapa;
    private int pasosComun;
    private Tipo tipo;
    private boolean conexionTerminada;
    private int contadorConexion;
    private int EnergiaTotal;
    
    class Datosvehiculo{
        public Tipo tipoDeVehiculo; 
        public int Bateria;  
        public int x; 
        public int y;
        private JSONArray sensor;
        private boolean visto;
 };

    /**
    *
    * @author Joaquín
    */
    public Pizarra(AgentID aid) throws Exception {
        super(aid);
        outbox = null;
        inbox = null;  
        finalizar = false;
        mapa_compartido = new int [1000][1000];
        mapa= new JSONArray();
        pasosComun = 0;
        objetivoEncontrado=false;
        conexionTerminada=false;
        contadorConexion=0;
        vehiculos = new HashMap<String, Datosvehiculo>();
        EnergiaTotal=0;
    }
    
    /**
    *
    * @author Joaquin
    */
    public void conexion() throws JSONException, InterruptedException{
        envio = new JSONObject();
        envio.put("world","map1");
        enviar_mensaje(envio.toString(), "Achernar", ACLMessage.SUBSCRIBE);
        recibir_mensaje();
        recibir_mensaje();
        envio = new JSONObject();
        envio.put("ID",conversacion_id);
        envio.put("Mapa",mapa);
        envio.put("Pasos", pasosComun);
        for(int i = 1; i <= 4; i++)
            enviar_mensaje(envio.toString(), "vehiculo"+i, ACLMessage.REQUEST);
        
        
    }
    
    /**
    *
    * @author Alex Sergio Salomé Joaquín
    */
    public void enviar_mensaje(String mensaje, String receptor, int performativa){
        System.out.println("Pizarra Envia: " +mensaje+receptor);
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(mensaje);
        outbox.setPerformative(performativa);
        this.send(outbox);
    }
    
    /**
    *
    * @author Alex Sergio Salomé Joaquín
    */
    public void enviar_mensaje(String mensaje, AgentID receptor, int performativa){
        System.out.println("Pizarra envia: " +mensaje + " a "+receptor);
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(receptor);
        outbox.setContent(mensaje);
        outbox.setPerformative(performativa);
        this.send(outbox);
    }
    
    /**
    *
    * @author Alex Sergio Salomé Joaquín
    */
    public void recibir_mensaje() throws InterruptedException, JSONException{
        inbox = receiveACLMessage();
        recepcion = new JSONObject(inbox.getContent());
        String recepcion_plano = recepcion.toString();
        System.out.println("Pizarra: " + recepcion_plano);
        if(inbox.getPerformativeInt()==ACLMessage.INFORM ){
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
    public void moverAgente(String nombreAgent, String objectivo) throws JSONException{
                                envio = new JSONObject();
                              envio.put("Accion","Buscar");
                              envio.put("BuscaObjetivo", mapa);
                              envio.put("Pasos", pasosComun);
                              //Enviamos el siguiente movimiento
                              enviar_mensaje(envio.toString(),nombreAgent,ACLMessage.REQUEST);
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
            ///conexion
            if(recepcion.has("TipoVehiculo")){       
                
                       String t = recepcion.getString("TipoVehiculo");
                       
                         switch (t) {
                             case "CAMION":
                                tipo = Tipo.CAMION;
                             break;
                             case "COCHE":
                                tipo = Tipo.COCHE;
                             break;
                             case "AEREO":
                                tipo = Tipo.AEREO; 
                               break;
                         }
                          Datosvehiculo v= new Datosvehiculo();                       
                          v.tipoDeVehiculo=tipo; 
                          vehiculos.put(recepcion.getString("ID"),v);
                          
                          System.out.println("Vehiculo guardado en pizarra: " + inbox.getSender().toString());
                          contadorConexion++;
                          
                         if(contadorConexion>=4){
                             System.out.println("Conexion Terminada, los 4 vehiculos asignados" + inbox.getSender().toString());
                              //Enviamos el primer movimiento
                              Datosvehiculo vActualD0 = vehiculos.get(inbox.getSender().toString());
                               if(vActualD0.tipoDeVehiculo== Tipo.COCHE){
                              moverAgente(inbox.getSender().toString(),"ninguno");}
                             
                            }
                         
                }else if(contadorConexion>=4){
                    
                               Datosvehiculo vActualD = vehiculos.get(inbox.getSender().toString());
                               if(vActualD.tipoDeVehiculo== Tipo.COCHE){
                               moverAgente(inbox.getSender().toString(),"ninguno");}
                               
                }
                /////////mensaje de que encuentra objetivo//////////
                if(recepcion.has("visto")){
                    
                       String vActualID = inbox.getSender().toString();
                       Datosvehiculo vActualDatos = vehiculos.get(vActualID);
                       vActualDatos.visto = recepcion.getBoolean("visto");
                      // vActualDatos.sensor = recepcion.getJSONArray("MapaAux");
                       vActualDatos.x = recepcion.getInt("x");
                       vActualDatos.y = recepcion.getInt("y");
                       vActualDatos.Bateria = recepcion.getInt("Bateria");
                       EnergiaTotal = recepcion.getInt("energy");
                       vehiculos.put(vActualID, vActualDatos);
                       
                       
                    if(recepcion.getBoolean("visto")){
                        System.out.println("enhorabuena,Objetivo encontrado por = " + vActualID);
                        objetivoEncontrado=true;
                    }
                }
        }
        /*
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
         //finalizar=true;
        }
        ///////////////////////Fin-Baterias agotadas y refuel Agotado///////////////////
        */
    }
    
}
