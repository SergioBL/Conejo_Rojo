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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author salome
 */
public class Vehiculo extends SingleAgent{
    
    private int fuelrate;
    private int bateria;
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
    private AgentID nombreVehiculo;
    private String inReplyTo;
    private int paso;
    private int energy;
    private boolean goal;
    private int scanner[][];

    /**
    *
    * @author 
    */
    public Vehiculo(AgentID aid) throws Exception {
        super(aid);
        outbox = null;
        inbox = null;  
        finalizar = false;
        inReplyTo = null;
        conversacion_id = null;
        nombreVehiculo= aid;
        mapa = new int[1000][1000];
        paso = 0;
        scanner = null;
        inicializarMapa();
    }
    
    /**
    *
    * @author Alex
    */
    public boolean conexion() throws InterruptedException, JSONException{
        //Recibimos el request de pizarra
        recibir_mensaje();
        //Construimos el mensaje
        envio = new JSONObject();
        envio.put("command","checkin");
        //Enviamos el checkin al servidor
        enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
        //Recibimos la respuesta del servidor
        recibir_mensaje();
        //Si da error, la conexión falla
        if(inbox.getPerformativeInt()==ACLMessage.REFUSE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
            enviar_mensaje(recepcion.getString("details"), "pizarra", ACLMessage.REFUSE);
            return false;
        }//En caso contrario, la conexión tiene exito y enviamos a pizarra quien somos y que somos
        else{
            JSONObject datos = recepcion.getJSONObject("Capabilities"); 
            fuelrate = recepcion.getInt("fuelrate");
            range = recepcion.getInt("range");
            switch (range) {
                case 7:
                    tipo = Tipo.CAMION;
                    break;
                case 5:
                    tipo = Tipo.COCHE;
                    break;
                default:
                    tipo = Tipo.AEREO;
                    break;
            }
            radar = new int [range][range];
            envio = new JSONObject();
            envio.put("TipoVehiculo",tipo);
            envio.put("ID",nombreVehiculo);
            enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
            recibir_mensaje();
            if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                enviar_mensaje(" ", "pizarra", ACLMessage.REFUSE);
                return false;
            }else
                actualizarDatos();
            
            envio = new JSONObject();
            envio.put("TipoVehiculo",tipo);
            envio.put("ID",nombreVehiculo);
            //Avisamos a pizarra
            enviar_mensaje(envio.toString(), "pizarra", ACLMessage.INFORM);
            return true;
        }
            
    }
    
    /**
    *
    * @author Alex
    */
    public void actualizarDatos() throws JSONException{
        JSONObject datos = recepcion.getJSONObject("result");
        bateria = datos.getInt("battery");
        gps_x = datos.getInt("x");
        gps_y = datos.getInt("y");
        energy = datos.getInt("energy");
        goal = datos.getBoolean("goal");
        JSONArray rad = datos.getJSONArray("sensor");
        for(int i = 0; i < rad.length(); i+=range)
            for(int j = 0; j < range; j++)
                radar[i/range][j] = rad.getInt(i+j);
        
        System.out.print("Reconocimiento: actualizo radar ");
        for(int i = 0; i < range; i++)
            for(int j = 0; j < range; j++)
                System.out.print(radar[i][j] + ", ");
        
        actualizarMapaComun();
    }
    
    public void inicializarMapa(){
        for(int i=0; i<1000; i++){
            for(int j=0; j<1000; j++){
                mapa[i][j] = -1;
            }
        }
    }
    
    /**
    *
    * @author Alex
    */
    public void actualizarMapaComun(){
        int medio;
        if(range==7)
            medio=3;
        else if(range==5)
            medio=2;
        else
            medio=1;
        for(int i = 0; i < range; i++)
            for(int j = 0; j < range; j++){
                if(radar[i][j]==1)
                    mapa[1000/2 + gps_y - medio + i][1000/2 + gps_x - medio + j] = 50000;
            }
        paso++;
        mapa[1000/2 + gps_y][1000/2 + gps_x] = paso;
    }
    
    /**
    *
    * @author Alex
    */
    public void obtieneMapaComun() throws JSONException{
        JSONArray map;
        if(recepcion.has("ID"))
            map = recepcion.getJSONArray("Mapa");
        else
            map = recepcion.getJSONArray("BuscaObjetivo");
        paso = recepcion.getInt("Pasos");
         for(int i = 0; i < map.length(); i+=1000)
            for(int j = 0; j < 1000; j++)
                mapa[i/1000][j] = map.getInt(i+j);
    }
    
    public void obtieneScanner() throws JSONException{
        JSONArray scan;
        scan = recepcion.getJSONArray("Scanner");
        
        for(int i = 0; i < scan.length(); i+=1000)
            for(int j = 0; j < 1000; j++)
                scanner[i/1000][j] = scan.getInt(i+j);
    }
    
    /**
    *
    * @author Alex Sergio Salomé Joaquín
    */
    public void enviar_mensaje(String mensaje, String receptor, int performativa){
        System.out.println("Vehiculo envia: " +mensaje + " a "+receptor);
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        //Para contestar con la id de la conversacion
        if(conversacion_id!=null)
            outbox.setConversationId(conversacion_id);
        if(inReplyTo!=null && receptor.equals("Achernar"))
            outbox.setInReplyTo(inReplyTo);
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(mensaje);
        outbox.setPerformative(performativa);
        this.send(outbox);
    }
    
    /**
    *
    * @author Alex Sergio Salomé Joaquín
    * @throws java.lang.InterruptedException
    */
    public void recibir_mensaje() throws InterruptedException, JSONException{
        inbox = receiveACLMessage();
        recepcion = new JSONObject(inbox.getContent());
        String recepcion_plano = recepcion.toString();
        System.out.println("Vehiculo: " + recepcion_plano);
        if(inbox.getPerformativeInt()==ACLMessage.REQUEST && conversacion_id == null){
            if(recepcion.has("ID")){
                conversacion_id = recepcion.getString("ID");
                obtieneMapaComun();
            }
        }else
            inReplyTo = inbox.getReplyWith();
    }
    
    /**
    *
    * @author Alex
    */
    @Override
    public void execute(){
        try {
            if(conexion()) 
                while(!finalizar){
                    try {
                        recibir_mensaje();
                        actuar();
                    } catch (InterruptedException | JSONException ex) {
                    Logger.getLogger(Pizarra.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
        } catch (InterruptedException | JSONException ex) {
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
            System.out.println("Vehiculo muerto");
        } finally {
            super.finalize();
        }
    }

    
    
    public String movimientoTerrestreConScanner(){
        String movimiento = "";
        int menor_paso = 50000;
        float menor_distancia = 50000;
        
        if(radar[1][1] != 1){
            movimiento = "moveNW";                      
            menor_paso = mapa[1000/2 + gps_y-1][1000/2 + gps_x-1];
            menor_distancia = scanner[1][1];
        }

        if(radar[1][2] != 1 && mapa[1000/2 + gps_y-1][1000/2 + gps_x] <= menor_paso && radar[1][2] != 2){
            if(mapa[1000/2 + gps_y-1][1000/2 + gps_x] == menor_paso){
                if(scanner[1][2] < menor_distancia){
                    movimiento = "moveN";
                    menor_distancia = scanner[1][2];
                }
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[1000/2 + gps_y-1][1000/2 + gps_x];
                menor_distancia = scanner[1][2];
            }  

        }             

        if(radar[1][3] != 1 && mapa[1000/2 + gps_y-1][1000/2 + gps_x+1] <= menor_paso && radar[1][3] != 2){
            if(mapa[1000/2 + gps_y-1][1000/2 + gps_x+1] == menor_paso){
                if(scanner[1][3] < menor_distancia){
                    movimiento = "moveNE";
                    menor_distancia = scanner[1][3];
                }
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[1000/2 + gps_y-1][1000/2 + gps_x+1];
                menor_distancia = scanner[1][3];
            }    

        }

       if(radar[2][1] != 1 && mapa[1000/2 + gps_y][1000/2 + gps_x-1] <= menor_paso && radar[2][1] != 2){
            if(mapa[1000/2 + gps_y][1000/2 + gps_x-1] == menor_paso){
                if(scanner[2][1] < menor_distancia){
                    movimiento = "moveW";
                    menor_distancia = scanner[2][1];
                }
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[1000/2 + gps_y][1000/2 + gps_x-1];
                menor_distancia = scanner[2][1];
            }  

        }

        if(radar[2][3] != 1 && mapa[1000/2 + gps_y][1000/2 + gps_x+1] <= menor_paso && radar[2][3] != 2){
            if(mapa[1000/2 + gps_y][1000/2 + gps_x+1] == menor_paso){
                if(scanner[2][3] < menor_distancia){
                    movimiento = "moveE";         
                    menor_distancia = scanner[2][3];
                }
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[1000/2 + gps_y][1000/2 + gps_x+1];
                menor_distancia = scanner[2][3];
            }   

        }

        if(radar[3][1] != 1 && mapa[1000/2 + gps_y+1][1000/2 + gps_x-1] <= menor_paso && radar[3][1] != 2){
            if(mapa[1000/2 + gps_y+1][1000/2 + gps_x-1] == menor_paso){
                if(scanner[3][1] < menor_distancia){
                    movimiento = "moveSW";  
                    menor_distancia = scanner[3][1];
                }
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[1000/2 + gps_y+1][1000/2 + gps_x-1];
                menor_distancia = scanner[3][1];
            }                    
        }

       if(radar[3][2] != 1 && mapa[1000/2 + gps_y+1][1000/2 + gps_x] <= menor_paso && radar[3][2] != 2){
            if(mapa[1000/2 + gps_y+1][1000/2 + gps_x] == menor_paso){
                if(scanner[3][2] < menor_distancia){
                    movimiento = "moveS";
                    menor_distancia = scanner[3][2];
                }
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[1000/2 + gps_y+1][1000/2 + gps_x];
                menor_distancia = scanner[3][2];
            }   

        }

        if(radar[3][3] != 1 && mapa[1000/2 + gps_y+1][1000/2 + gps_x+1] <= menor_paso && radar[3][3] != 2){
            if(mapa[1000/2 + gps_y+1][1000/2 + gps_x+1] == menor_paso){
                if(scanner[3][3] < menor_distancia){
                    movimiento = "moveSE";
                    menor_distancia = scanner[3][3];
                }
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[1000/2 + gps_y+1][1000/2 + gps_x+1];
                menor_distancia = scanner[3][3];
            }   

        }
        
        movimiento = comprobarObjetivoAlrededor(movimiento);
        
        return movimiento;
    }
    
    public String movimientoAereoConScanner(){
        String movimiento = "";
        int menor_paso = 50000;
        int menor_distancia = 50000;
        
        if(radar[1][1] != 2){
            movimiento = "moveNW";                      
            menor_paso = mapa[1000/2 + gps_y-1][1000/2 + gps_x-1];
            menor_distancia = scanner[1][1]; 
        }
                              

        if(mapa[1000/2 + gps_y-1][1000/2 + gps_x] <= menor_paso && radar[1][2] != 2){
            if(mapa[1000/2 + gps_y-1][1000/2 + gps_x] == menor_paso){
                if(scanner[1][2] < menor_distancia){
                    movimiento = "moveN";
                    menor_distancia = scanner[1][2];
                }
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[1000/2 + gps_y-1][1000/2 + gps_x];
                menor_distancia = scanner[1][2];
            }  

        }             

        if(mapa[1000/2 + gps_y-1][1000/2 + gps_x+1] <= menor_paso && radar[1][3] != 2){
            if(mapa[1000/2 + gps_y-1][1000/2 + gps_x+1] == menor_paso){
                if(scanner[1][3] < menor_distancia){
                    movimiento = "moveNE";
                    menor_distancia = scanner[1][3];
                }
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[1000/2 + gps_y-1][1000/2 + gps_x+1];
                menor_distancia = scanner[1][3];
            }    

        }

       if(mapa[1000/2 + gps_y][1000/2 + gps_x-1] <= menor_paso && radar[2][1] != 2){
            if(mapa[1000/2 + gps_y][1000/2 + gps_x-1] == menor_paso){
                if(scanner[2][1] < menor_distancia){
                    movimiento = "moveW";
                    menor_distancia = scanner[2][1];
                }
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[1000/2 + gps_y][1000/2 + gps_x-1];
                menor_distancia = scanner[2][1];
            }  

        }

        if(mapa[1000/2 + gps_y][1000/2 + gps_x+1] <= menor_paso && radar[2][3] != 2){
            if(mapa[1000/2 + gps_y][1000/2 + gps_x+1] == menor_paso){
                if(scanner[2][3] < menor_distancia){
                    movimiento = "moveE";         
                    menor_distancia = scanner[2][3];
                }
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[1000/2 + gps_y][1000/2 + gps_x+1];
                menor_distancia = scanner[2][3];
            }   

        }

        if(mapa[1000/2 + gps_y+1][1000/2 + gps_x-1] <= menor_paso && radar[3][1] != 2){
            if(mapa[1000/2 + gps_y+1][1000/2 + gps_x-1] == menor_paso){
                if(scanner[3][1] < menor_distancia){
                    movimiento = "moveSW";  
                    menor_distancia = scanner[3][1];
                }
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[1000/2 + gps_y+1][1000/2 + gps_x-1];
                menor_distancia = scanner[3][1];
            }                    
        }

       if(mapa[1000/2 + gps_y+1][1000/2 + gps_x] <= menor_paso && radar[3][2] != 2){
            if(mapa[1000/2 + gps_y+1][1000/2 + gps_x] == menor_paso){
                if(scanner[3][2] < menor_distancia){
                    movimiento = "moveS";
                    menor_distancia = scanner[3][2];
                }
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[1000/2 + gps_y+1][1000/2 + gps_x];
                menor_distancia = scanner[3][2];
            }   

        }

        if(mapa[1000/2 + gps_y+1][1000/2 + gps_x+1] <= menor_paso && radar[3][3] != 2){
            if(mapa[1000/2 + gps_y+1][1000/2 + gps_x+1] == menor_paso){
                if(scanner[3][3] < menor_distancia){
                    movimiento = "moveSE";
                    menor_distancia = scanner[3][3];
                }
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[1000/2 + gps_y+1][1000/2 + gps_x+1];
                menor_distancia = scanner[3][3];
            }   

        }      
       
        movimiento = comprobarObjetivoAlrededor(movimiento);
       
        return movimiento;
    }
    
    public String comprobarObjetivoAlrededor(String ultimo_movimiento){
        String movimiento = ultimo_movimiento;
        
        if(radar[1][1] == 3){
            movimiento = "moveNW";
        }    

        if(radar[1][2] == 3){
            movimiento = "moveN";
        }

        if(radar[1][3] == 3){
            movimiento = "moveNE";
        }

        if(radar[2][1] == 3){
            movimiento = "moveW";
        }

        if(radar[2][3] == 3){
            movimiento = "moveE";
        }

        if(radar[3][1] == 3){
            movimiento = "moveSW";
        }

        if(radar[3][2] == 3){
            movimiento = "moveS";
        }

        if(radar[3][3] == 3){
            movimiento = "moveSE";
        }
        
        return movimiento;
    }
    
    /**
    *
    * @author
    * @throws org.codehaus.jettison.json.JSONException
    * @throws java.lang.InterruptedException
    */
    public void actuar() throws JSONException, InterruptedException{
        
        //Busqueda
        if(inbox.getPerformativeInt()==ACLMessage.REQUEST){ 
            if(recepcion.getString("Accion").equals("Buscar")){
                obtieneMapaComun();
                if(bateria<=1){
                    //Codigo de salomé
                }else{
                    //Pensar movimiento

                    String movimiento = "moveN";
                    envio = new JSONObject();
                    envio.put("command",movimiento);
                    //Nos movemos
                    enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                    recibir_mensaje();
                }

                //Comrpobamos que no ha habido error al hablar con el servidor
                if(inbox.getPerformativeInt()==ACLMessage.FAILURE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD || inbox.getPerformativeInt()==ACLMessage.REFUSE){
                        finalizar =true;
                        enviar_mensaje(recepcion.getString("details"),"pizarra",ACLMessage.REFUSE);
                }
                else{
                    //actualizamos los datos ya que nos hemos movido y se lo enviamos a pizarra
                    envio = new JSONObject();

                    enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                    recibir_mensaje();
                    if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD)
                        enviar_mensaje(recepcion.getString("details"),"pizarra",ACLMessage.REFUSE);
                    else
                        actualizarDatos();

                    int map[] = new int[1000000];
                    int k = 0;
                    for(int i = 0; i < 1000; i++)
                        for(int j = 0; j < 1000; j++, k++)
                            map[k] = mapa[i][j];

                    envio.put("MapaAux",map);
                    envio.put("x", gps_x);
                    envio.put("x", gps_y);
                    envio.put("energy", energy);
                    enviar_mensaje(envio.toString(), "pizarra", ACLMessage.INFORM);  
                }
            }
            else if(recepcion.getString("Accion").equals("LlegaObjetivo")){
                if(scanner == null){
                    obtieneScanner();
                    inicializarMapa();
                }
                
                while(!goal && bateria > 1){                                       
                    String movimiento = "";               
                    
                    if(tipo.equals(Tipo.CAMION) || tipo.equals(Tipo.COCHE)){
                        movimiento = movimientoTerrestreConScanner();
                    }
                    
                    else if(tipo.equals(Tipo.AEREO)){               
                        movimiento = movimientoAereoConScanner();                      
                    }                                   

                    envio = new JSONObject();
                    envio.put("command",movimiento);
                    enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                    recibir_mensaje();   
                    
                    if(inbox.getPerformativeInt()==ACLMessage.FAILURE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD || inbox.getPerformativeInt()==ACLMessage.REFUSE){
                        finalizar =true;
                        enviar_mensaje(recepcion.getString("details"),"pizarra",ACLMessage.REFUSE);
                    }
                    else{
                        
                        envio = new JSONObject();

                        enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                        recibir_mensaje();
                        if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD)
                            enviar_mensaje(recepcion.getString("details"),"pizarra",ACLMessage.REFUSE);
                        else
                            actualizarDatos();
                    }
                }
                
                if(goal){

                }
                else{
                    if(bateria <= 1){
                        
                    }
                }
            }   
        }
            
    }

}
