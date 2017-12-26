/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
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
    
    private Map<String, DatosVehiculo> vehiculos;
    private boolean finalizar;
    private String conversacion_id;
    private boolean objetivoEncontrado;
    private boolean EnObjetivo;//asociarlo a vehiculo
    private JSONArray mapa;
    private JSONArray scanner;
    private int pasosComun;
    private Tipo tipo;
    private int EnergiaTotal;
    private int NvehiculosObjetivo;
    private MyDrawPanel m;
    private JFrame jframe;
    
    class DatosVehiculo{
        public Tipo tipoVehiculo; 
        public int Bateria;  
        public int x; 
        public int y;
        private boolean visto;
        private boolean EnObjetivo;
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
        mapa_compartido = new int [500][500];
        mapa= new JSONArray();
        for(int i=0;i<250000;i++)
            mapa.put(0);
        scanner_compartido = new int [500][500];
        pasosComun = 0;
        objetivoEncontrado=false;
        vehiculos = new HashMap<String, DatosVehiculo>();
        EnergiaTotal=0;
        NvehiculosObjetivo=0;
    }
    
    /**
    *
    * @author Joaquin Alex
    */
    public void conexion() throws JSONException, InterruptedException{
        envio = new JSONObject();
        envio.put("world","map1");
        enviar_mensaje(envio.toString(), "Achernar", ACLMessage.SUBSCRIBE);
        recibir_mensaje();
        if(recepcion.has("trace")){
            recibir_mensaje();
        }
        envio = new JSONObject();
        envio.put("ID",conversacion_id);       
        CompresorArray c = new CompresorArray(mapa);
        envio.put("Mapa",c.getStringComprimido());
        envio.put("Pasos", pasosComun);
        for(int i = 1; i <= 4; i++){
            int aux = i+8;
            enviar_mensaje(envio.toString(), "vehiculo"+aux, ACLMessage.REQUEST);
            recibir_mensaje();
            
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
                DatosVehiculo v= new DatosVehiculo();                       
                v.tipoVehiculo=tipo;
                vehiculos.put(recepcion.getString("ID"),v);
                          
                System.out.println("Vehiculo guardado en pizarra: " + inbox.getSender().toString());
                
            }
          
            System.out.println("Conexion Terminada, los 4 vehiculos asignados" + inbox.getSender().toString());
            int coches = 0;
            for(Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()) {
                DatosVehiculo vehiculo = entry.getValue();
                if(vehiculo.tipoVehiculo==Tipo.COCHE)
                    coches++;
            }
            
            if(coches==0)
                System.out.println("No hay coches");
            else
                System.out.println("Hay " + coches +" tipo coche" );
           
        }
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
        System.out.println("Pizarra envia: " + " a "+receptor);
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
            //System.out.println("Conversacion: " + inbox.getConversationId());
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
    * @author Joaquin
    */
    public void moverAgente(String nombreAgent) throws JSONException{
        envio = new JSONObject();
        envio.put("Accion","Buscar");
        CompresorArray c = new CompresorArray(mapa);
        String mapaComprimido = c.getStringComprimido();
        envio.put("BuscaObjetivo", mapaComprimido);
        envio.put("Pasos", pasosComun);
        //Enviamos el siguiente movimiento
        enviar_mensaje(envio.toString(),nombreAgent,ACLMessage.REQUEST);
    }
    
    /**
     *  @author Alex
     */
    public void rellenarMatrizScanner(int x, int y){
        int pos_x = x;
        int pos_y = y;
        scanner_compartido[pos_y][pos_x] = 0;
        for(int i = 0; i < 500; i++)
            for(int j = 0; j < 500; j++){
                int distancia_x = pos_x - j;
                int distancia_y = pos_y -i;
                if(distancia_x<0)
                    distancia_x*=-1;
                if(distancia_y<0)
                    distancia_y*=-1;
                if(distancia_x>distancia_y)
                    scanner_compartido[i][j] = distancia_x;
                else
                    scanner_compartido[i][j] = distancia_y;
            }
    }
    
    /**
    *
    * @author Joaquin
    */
    public void moverAgenteObjetivo(String nombreAgent) throws JSONException{
        envio = new JSONObject();
        envio.put("Accion","LlegaObjetivo");
        for(int i = 0; i < 500; i++)
            for(int j = 0; j < 500; j++)
                scanner.put(scanner_compartido[i][j]);
            
        CompresorArray c = new CompresorArray(scanner);
        String arrayComprimido = c.getStringComprimido();
        envio.put("Scanner", arrayComprimido);
        //Enviamos el siguiente movimiento
        enviar_mensaje(envio.toString(),nombreAgent,ACLMessage.REQUEST);
    }
    /**
    *
    * @author Joaquin
    */
    public void siguienteVehiculoObjetivo()throws JSONException{
        System.out.println("numero de vehiculos" +vehiculos.size());
        int distancia = 10000;
        String enviar = "";
        for (Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()) {
            DatosVehiculo vehiculo = entry.getValue();
            if(!vehiculo.EnObjetivo){
               int dis = scanner_compartido[vehiculo.y][vehiculo.x];
               if(dis>distancia){
                   distancia = dis;
                   enviar = entry.getKey();
               }
            }           
        }
        if(!enviar.equals(""))
            moverAgenteObjetivo(enviar);
    }
    
    /**
    *
    * @author Joaquin Alex
    */
    public void buscarObjetivo()throws JSONException, InterruptedException{
        for (Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()) {
            DatosVehiculo vehiculo = entry.getValue();
            //Solo buscan los coches
            if(!objetivoEncontrado && vehiculo.tipoVehiculo.equals(tipo.COCHE)){
                moverAgente(entry.getKey());
                recibir_mensaje();

                if(inbox.getPerformativeInt()==ACLMessage.INFORM){

                    String vActualID = inbox.getSender().toString();
                    DatosVehiculo vActualDatos = vehiculos.get(vActualID);
                    
                    String mapaComprimido = recepcion.getString("MapaAux");
                    CompresorArray c = new CompresorArray(mapaComprimido);
                    mapa = c.getArraySinComprimir();
                    pasosComun = recepcion.getInt("Pasos");
                    obtieneMapaComun();
                    
                    vActualDatos.x = recepcion.getInt("x");
                    vActualDatos.y = recepcion.getInt("y");
                    vActualDatos.Bateria = recepcion.getInt("Bateria");
                    vActualDatos.EnObjetivo = false;
                    EnergiaTotal = recepcion.getInt("energy");
                    vehiculos.put(vActualID, vActualDatos);
                    if(recepcion.getBoolean("visto")){
                        System.out.println("enhorabuena,Objetivo encontrado por = " + inbox.getSender().toString());
                        objetivoEncontrado=true;
                        //Enviamos primero al mas cercano
                        rellenarMatrizScanner(recepcion.getInt("o_x"),recepcion.getInt("o_y"));
                        moverAgenteObjetivo(inbox.getSender().toString());
                    }
                }else{
                    System.out.println("No se mueve bien el coche");
                }
            }
        }
    }
    /**
    *
    * @author Joaquin
    */
    public void obtieneMapaComun() throws JSONException{
        
         for(int i = 0; i < mapa.length(); i+=500)
            for(int j = 0; j < 500; j++)
                mapa_compartido[i/500][j] = mapa.getInt(i+j);
    }
    /**
    *
    * @author Joaquin
    */
    @Override
    public void execute(){
        try {
            conexion();
            jframe = new JFrame();
            m = new MyDrawPanel(mapa_compartido);
            jframe.add(m);
            jframe.setSize(mapa_compartido.length, mapa_compartido.length);
            jframe.setVisible(true);
            jframe.setTitle("Gugel");
            while(!finalizar){
                actuar();
                m.Update(mapa_compartido);
                m.repaint();
            }
        } catch (JSONException | InterruptedException ex) {
            Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    *
    * @author Joaquin
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
    * @author Joaquin
    */
    @Override
    public void init(){
        System.out.println("Pizarra: vivo");
    }
    
    /**
    *
    * @author Joaquin Alex
    */
    public void actuar() throws JSONException, InterruptedException{
          
        ////////////////////////Busqueda////////////////////////
        if(!objetivoEncontrado){
            buscarObjetivo();
        }
        
        /////////////////////Fin-Busqueda///////////////////////////
        
        /////////////////////Llegada-Obretivo///////////////////////
        
        if(objetivoEncontrado && !EnObjetivo){
            if(recepcion.has("EnObjetivo")){
                 if(recepcion.getBoolean("EnObjetivo")){
                    System.out.println("vehiculo " + inbox.getSender().toString()+" enObjetivo");
                    DatosVehiculo vActualDato = vehiculos.get(inbox.getSender().toString());
                    vActualDato.EnObjetivo = recepcion.getBoolean("EnObjetivo");
                    vehiculos.put(inbox.getSender().toString(), vActualDato);
                    NvehiculosObjetivo++;
                 }}
            if(NvehiculosObjetivo<=4){
                siguienteVehiculoObjetivo();
                NvehiculosObjetivo++;
            }else if(NvehiculosObjetivo>=4){
                 EnObjetivo=true;
                 System.out.println("Todos o casi todos en objetivo");
                 
                 envio = new JSONObject();
                 envio.put("","");
                 enviar_mensaje(envio.toString(),"achernar",ACLMessage.CANCEL);
                 
                 
            }

        }
        ////////////////////Fin-llegadaObjetivo////////////////////////////
        
        ///////////////////////fin///////////////////
        
        if(recepcion.has("trace")){
            System.out.println("Programa Terminado la traza es =  " + recepcion.getJSONArray("trace"));
        }
        ///////////////////////Fin-fin///////////////////
        
    }
    
}
