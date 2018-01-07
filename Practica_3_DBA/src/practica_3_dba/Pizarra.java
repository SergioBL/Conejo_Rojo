/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private String mapa_explorar;
    
    private Map<String, DatosVehiculo> vehiculos;
    private boolean finalizar;
    private String conversacion_id;
    private boolean objetivoEncontrado;
    private boolean EnObjetivo;//asociarlo a vehiculo
    private JSONArray mapa;
    private int pasosComun;
    private Tipo tipo;
    private int EnergiaTotal;
    private int NvehiculosObjetivo;
    private DibujarMapa m;
    private JFrame jframe;
    private Memoria memoria;
    
    class DatosVehiculo{
        public Tipo tipoVehiculo; 
        public int Bateria;  
        public int x; 
        public int y;
        public boolean visto;
        public boolean EnObjetivo = false;
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
        mapa_compartido = new int [520][520];
        mapa= new JSONArray();
        for(int i=0;i<270400;i++)
            mapa.put(0);
        scanner_compartido = new int [520][520];
        pasosComun = 0;
        objetivoEncontrado=false;
        vehiculos = new HashMap<String, DatosVehiculo>();
        EnergiaTotal=0;
        NvehiculosObjetivo=0;
        mapa_explorar = "map7";
        memoria= new Memoria(mapa_explorar);
    }
    
    /**
    *
    * @author Joaquin Alex Joaquin
    */
    public void conexion() throws JSONException, InterruptedException{
        envio = new JSONObject();
        envio.put("world",mapa_explorar);
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
            int aux = i;
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
                v.x = recepcion.getInt("x");
                v.y = recepcion.getInt("y");
                v.EnObjetivo = false;
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
            finalizar = true;
        }else if(inbox.getPerformativeInt() == ACLMessage.REFUSE){
            System.out.println("Pizarra REFUSE: " + recepcion_plano);
            finalizar = true;
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
     *  @author Alex Joaquin
     */
    public void rellenarMatrizScanner(int x, int y) throws IOException{
        int pos_x = x;
        int pos_y = y;
        
        if(!memoria.leer()){
             memoria.escribir(Integer.toString(x),Integer.toString(y));
              System.out.println("Escribiendo objetivo en TXT");
        }else{
              memoria.leer(); 
              pos_x= memoria.getX();
              pos_y = memoria.getY();
              System.out.println("Objetivo cargado de memoria x = "+pos_x + " e y = "+ pos_y);
        }
        scanner_compartido[pos_y][pos_x] = 0;
        for(int i = 0; i < 520; i++)
            for(int j = 0; j < 520; j++){
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
    * @author Joaquin Sergio Alex Alvaro
    */
    public void moverAgenteObjetivo(String nombreAgent) throws JSONException, InterruptedException{
        envio = new JSONObject();
        envio.put("Accion","LlegaObjetivo");
        JSONArray scanner = new JSONArray();
        for(int i = 0; i < 520; i++)
            for(int j = 0; j < 520; j++)
                scanner.put(scanner_compartido[i][j]);
        JSONArray ocupados = new JSONArray();
        for (Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()){
            DatosVehiculo vehiculo = entry.getValue();
            if(vehiculo.EnObjetivo){
                JSONObject punto = new JSONObject();
                punto.put("x",vehiculo.x);
                punto.put("y",vehiculo.y);
                ocupados.put(punto);
            }
        }
        envio.put("Ocupados", ocupados);
        envio.put("Scanner", scanner);
        //Enviamos el siguiente movimiento
        enviar_mensaje(envio.toString(),nombreAgent,ACLMessage.REQUEST);
        recibir_mensaje();
    }
    /**
    *
    * @author Joaquin Alex Alvaro
    */
    public void siguienteVehiculoObjetivo()throws JSONException, InterruptedException{
        int distancia = 9000000;
        String enviar = "";
        for (Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()) {
            DatosVehiculo vehiculo = entry.getValue();
            if(!vehiculo.EnObjetivo){
               int dis = scanner_compartido[vehiculo.y][vehiculo.x];
               int gasta = 1;
               if(vehiculo.tipoVehiculo == Tipo.CAMION)
                   gasta = 4;
               dis *= gasta;
               if(dis<distancia){
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
    * @author Joaquin Alex Alvaro
    */
    public void buscarObjetivo()throws JSONException, InterruptedException, IOException{
        for (Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()) {
            DatosVehiculo vehiculo = entry.getValue();
            //Solo buscan los coches
            if(!objetivoEncontrado && vehiculo.tipoVehiculo == Tipo.COCHE){
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
                    m.Actualizar(mapa_compartido);
                    m.repaint();
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
        
         for(int i = 0; i < mapa.length(); i+=520)
            for(int j = 0; j < 520; j++)
                mapa_compartido[i/520][j] = mapa.getInt(i+j);
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
            m = new DibujarMapa(mapa_compartido);
            jframe.add(m);
            jframe.setSize(mapa_compartido.length, mapa_compartido.length);
            jframe.setVisible(true);
            jframe.setTitle("Mapa compartido");
            while(!finalizar){
                actuar();
            }
        } catch (JSONException | InterruptedException ex) {
            Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
            jframe.dispose();
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
    * @author Joaquin Alex Alvaro
    */
    public void actuar() throws JSONException, InterruptedException, IOException{
          
        ////////////////////////Busqueda////////////////////////
        if(memoria.leer()){
            objetivoEncontrado=true;
            System.out.println("OBJETIVO ENCONTRADO ANTERIORMENTE");
            this.rellenarMatrizScanner(memoria.getX(),memoria.getY());
        }
        
        if(!objetivoEncontrado){
            buscarObjetivo();
        }
        
        if(objetivoEncontrado){
            if(recepcion.has("EnObjetivo")){
                 if(recepcion.getBoolean("EnObjetivo")){
                    System.out.println("vehiculo " + inbox.getSender().toString()+" enObjetivo");
                    DatosVehiculo vActualDato = vehiculos.get(inbox.getSender().toString());
                    vActualDato.EnObjetivo = recepcion.getBoolean("EnObjetivo");
                    vActualDato.x = recepcion.getInt("x_o");
                    vActualDato.y = recepcion.getInt("y_o");
                    vehiculos.put(inbox.getSender().toString(), vActualDato);
                    System.out.println("Pizarra  -  HashVehiculos actualizados");
                 }
            }
            if(NvehiculosObjetivo<4){
                siguienteVehiculoObjetivo();
                NvehiculosObjetivo++;
            }else if(NvehiculosObjetivo==4){
                EnObjetivo=true;
                System.out.println("Todos en objetivo");
                finalizar=true;
                envio = new JSONObject();
                envio.put("","");
                enviar_mensaje(envio.toString(),"Achernar",ACLMessage.CANCEL);
                recibir_mensaje();
                recibir_mensaje();
                if(recepcion.has("trace")){
                    JSONArray traza= recepcion.getJSONArray("trace");
                    byte traza_bytes[]=new byte[traza.length()];
                    for(int x=0; x<traza_bytes.length; x++){
                        traza_bytes[x]=(byte) traza.getInt(x);
                    }
                    String tipos_vehiculos = "";
                    boolean primero = true;
                    for(Map.Entry<String, DatosVehiculo> entry : vehiculos.entrySet()) {
                        DatosVehiculo v = entry.getValue();
                        if(!primero)
                            tipos_vehiculos += ",";
                        tipos_vehiculos += v.tipoVehiculo.toString();
                        primero = false;
                    }
                    try (FileOutputStream fos = new FileOutputStream(mapa_explorar+"_"+conversacion_id+"_"+tipos_vehiculos+".png")) {
                        fos.write(traza_bytes);
                    }catch (IOException e){
                        System.err.println("Error en traza");
                    }
                }
            }
        }
    }
    
}
