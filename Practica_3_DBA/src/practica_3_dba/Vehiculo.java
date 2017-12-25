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
    private String nombre;
    private boolean visto;
    private int objetivo_x;
    private int objetivo_y;
    
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
        mapa = new int[500][500];
        paso = 0;
        scanner = null;
        //this.nombre = nombre;
        inicializarMapa();
    }
    
    /**
    *
    * @author Alex
    */
    public boolean conexion() throws InterruptedException, JSONException{
        //Recibimos el request de pizarra2
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
             enviar_mensaje(recepcion.getString("details"), "pizarra2", ACLMessage.REFUSE);
            return false;
        }//En caso contrario, la conexión tiene exito y enviamos a pizarra2 quien somos y que somos
        else{
            JSONObject datos = recepcion.getJSONObject("capabilities"); 
            fuelrate = datos.getInt("fuelrate");
            range = datos.getInt("range");
            switch (range) {
                case 11:
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
            enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
            recibir_mensaje();
            if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                enviar_mensaje(" ", "pizarra2", ACLMessage.REFUSE);
                return false;
            }else
                actualizarDatos();
            
            envio = new JSONObject();
            envio.put("TipoVehiculo",tipo.toString());
            envio.put("ID",nombreVehiculo);
            //Avisamos a pizarra2
            enviar_mensaje(envio.toString(), "pizarra2", ACLMessage.INFORM);
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
        
        /*System.out.print("Vehiculo: actualizo radar ");
        for(int i = 0; i < range; i++)
            for(int j = 0; j < range; j++)
                System.out.print(radar[i][j] + ", ");*/
        
        actualizarMapaComun();
    }
    
    /**
     *  @author Alex
     */
    public void obtenerPosicionObjetivo(){
        for(int i = 0; i < 500; i++)
            for(int j = 0; j < 500; j++){
                if(mapa[i][j]==3){
                    objetivo_x = j;
                    objetivo_y = i;
                }
            }
    }
    
    /**
    *
    * @author Alvaro
    */
    public void inicializarMapa(){
        for(int i=0; i<500; i++){
            for(int j=0; j<500; j++){
                mapa[i][j] = -1;
            }
        }
    }
    
    /**
    *
    * @author Alex
    */
    public void actualizarMapaComun(){
        //System.out.println("Vehiculo " +nombreVehiculo+" : actualizo mapa comun");
        int medio;
        if(range==11)
            medio=5;
        else if(range==5)
            medio=2;
        else
            medio=1;
        for(int i = 0; i < range; i++)
            for(int j = 0; j < range; j++){
                if(radar[i][j]==1 || radar[i][j] == 2)
                    mapa[500/2 + gps_y - medio + i][500/2 + gps_x - medio + j] = 50000;
            }
        paso++;
        mapa[500/2 + gps_y][500/2 + gps_x] = paso;
    }
    
    /**
    *
    * @author Alex
    */
    public void obtieneMapaComun() throws JSONException{
        //System.out.println("Vehiculo " +nombreVehiculo+" : obtengo mapa comun");
        CompresorArray c;
        if(recepcion.has("ID")){
            c = new CompresorArray(recepcion.getString("Mapa"));
        }else
            c = new CompresorArray(recepcion.getString("BuscaObjetivo"));
        JSONArray map = c.getArraySinComprimir();
        paso = recepcion.getInt("Pasos");
         for(int i = 0; i < map.length(); i+=500)
            for(int j = 0; j < 500; j++)
                mapa[i/500][j] = map.getInt(i+j);
    }
    
    public void obtieneScanner() throws JSONException{
        String sc = recepcion.getString("Scanner");
        
        CompresorArray c = new CompresorArray(sc);
        JSONArray scan = c.getArraySinComprimir();
        
        for(int i = 0; i < scan.length(); i+=500)
            for(int j = 0; j < 500; j++)
                scanner[i/500][j] = scan.getInt(i+j);
    }
    
    /**
    *
    * @author Alex Sergio Salomé Joaquín
    */
    public void enviar_mensaje(String mensaje, String receptor, int performativa){
        System.out.println("Vehiculo " + fuelrate + " envia: " +mensaje + " a "+receptor);
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
        System.out.println("Vehiculo " + fuelrate + " recibe: " + recepcion_plano);
        if(inbox.getPerformativeInt()==ACLMessage.REQUEST && conversacion_id == null){
            if(recepcion.has("ID")){
                conversacion_id = recepcion.getString("ID");
                obtieneMapaComun();
            }
        }else if(inbox.getPerformativeInt()!=ACLMessage.REQUEST)
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
                    Logger.getLogger(Vehiculo.class.getName()).log(Level.SEVERE, null, ex);
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
            System.out.println("Vehiculo " + fuelrate + " muerto");
        } finally {
            super.finalize();
        }
    }

    
    /**
    *
    * @author Alvaro
    */
    public String movimientoTerrestreConScanner(){
        String movimiento = "";
        int menor_paso = 50000;
        float menor_distancia = 50000;
        
        if(radar[1][1] != 1){
            movimiento = "moveNW";                      
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
            menor_distancia = scanner[1][1];
        }

        if(radar[1][2] != 1 && mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && radar[1][2] != 2){
            if(mapa[500/2 + gps_y-1][500/2 + gps_x] == menor_paso){
                if(scanner[1][2] < menor_distancia){
                    movimiento = "moveN";
                    menor_distancia = scanner[1][2];
                }
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                menor_distancia = scanner[1][2];
            }  

        }             

        if(radar[1][3] != 1 && mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && radar[1][3] != 2){
            if(mapa[500/2 + gps_y-1][500/2 + gps_x+1] == menor_paso){
                if(scanner[1][3] < menor_distancia){
                    movimiento = "moveNE";
                    menor_distancia = scanner[1][3];
                }
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                menor_distancia = scanner[1][3];
            }    

        }

       if(radar[2][1] != 1 && mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && radar[2][1] != 2){
            if(mapa[500/2 + gps_y][500/2 + gps_x-1] == menor_paso){
                if(scanner[2][1] < menor_distancia){
                    movimiento = "moveW";
                    menor_distancia = scanner[2][1];
                }
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                menor_distancia = scanner[2][1];
            }  

        }

        if(radar[2][3] != 1 && mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && radar[2][3] != 2){
            if(mapa[500/2 + gps_y][500/2 + gps_x+1] == menor_paso){
                if(scanner[2][3] < menor_distancia){
                    movimiento = "moveE";         
                    menor_distancia = scanner[2][3];
                }
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                menor_distancia = scanner[2][3];
            }   

        }

        if(radar[3][1] != 1 && mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && radar[3][1] != 2){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x-1] == menor_paso){
                if(scanner[3][1] < menor_distancia){
                    movimiento = "moveSW";  
                    menor_distancia = scanner[3][1];
                }
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                menor_distancia = scanner[3][1];
            }                    
        }

       if(radar[3][2] != 1 && mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && radar[3][2] != 2){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x] == menor_paso){
                if(scanner[3][2] < menor_distancia){
                    movimiento = "moveS";
                    menor_distancia = scanner[3][2];
                }
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                menor_distancia = scanner[3][2];
            }   

        }

        if(radar[3][3] != 1 && mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && radar[3][3] != 2){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x+1] == menor_paso){
                if(scanner[3][3] < menor_distancia){
                    movimiento = "moveSE";
                    menor_distancia = scanner[3][3];
                }
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                menor_distancia = scanner[3][3];
            }   

        }
        
        movimiento = comprobarObjetivoAlrededor(movimiento);
        
        return movimiento;
    }
    
    /**
    *
    * @author Alex
    */
    public String busquedaTerrestre(){
        String movimiento = "moveN";
        int menor_paso = 50000;
        int n, s, e, o, ne, no, se, so;
        
        if(tipo.equals(Tipo.COCHE)){
            no = radar[1][1];
            n = radar[1][2];
            ne = radar[1][3];
            o = radar[2][1];
            e = radar[2][3];
            so = radar[3][1];
            s = radar[3][2];
            se = radar[3][3];
        }else{
            no = radar[4][4];
            n = radar[4][5];
            ne = radar[4][6];
            o = radar[5][4];
            e = radar[5][6];
            so = radar[6][4];
            s = radar[6][5];
            se = radar[6][6];
        }
        
        if(no != 1 && no != 4 && no != 2){
            movimiento = "moveNW";                      
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
        }

        if(n != 1 && n != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && n != 2){
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
        }             

        if(ne != 1 && ne != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && ne != 2){
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
        }

       if(o != 1 && o != 4 && mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && o != 2){
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
        }

        if(e != 1 && e != 4 && mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && e != 2){
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
        }

        if(so != 1 && so != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && so != 2){
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
        }
        
       if(s != 1 && s != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && s != 2){
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
        }

        if(se != 1 && se != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && se != 2){
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
        }
        
        movimiento = comprobarObjetivoAlrededor(movimiento);
        
        return movimiento;
    }
    
    /**
    *
    * @author Alvaro
    */
    public String movimientoAereoConScanner(){
        String movimiento = "";
        int menor_paso = 50000;
        int menor_distancia = 50000;
        
        if(radar[1][1] != 2){
            movimiento = "moveNW";                      
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
            menor_distancia = scanner[1][1]; 
        }
                              

        if(mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && radar[1][2] != 2){
            if(mapa[500/2 + gps_y-1][500/2 + gps_x] == menor_paso){
                if(scanner[1][2] < menor_distancia){
                    movimiento = "moveN";
                    menor_distancia = scanner[1][2];
                }
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                menor_distancia = scanner[1][2];
            }  

        }             

        if(mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && radar[1][3] != 2){
            if(mapa[500/2 + gps_y-1][500/2 + gps_x+1] == menor_paso){
                if(scanner[1][3] < menor_distancia){
                    movimiento = "moveNE";
                    menor_distancia = scanner[1][3];
                }
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                menor_distancia = scanner[1][3];
            }    

        }

       if(mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && radar[2][1] != 2){
            if(mapa[500/2 + gps_y][500/2 + gps_x-1] == menor_paso){
                if(scanner[2][1] < menor_distancia){
                    movimiento = "moveW";
                    menor_distancia = scanner[2][1];
                }
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                menor_distancia = scanner[2][1];
            }  

        }

        if(mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && radar[2][3] != 2){
            if(mapa[500/2 + gps_y][500/2 + gps_x+1] == menor_paso){
                if(scanner[2][3] < menor_distancia){
                    movimiento = "moveE";         
                    menor_distancia = scanner[2][3];
                }
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                menor_distancia = scanner[2][3];
            }   

        }

        if(mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && radar[3][1] != 2){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x-1] == menor_paso){
                if(scanner[3][1] < menor_distancia){
                    movimiento = "moveSW";  
                    menor_distancia = scanner[3][1];
                }
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                menor_distancia = scanner[3][1];
            }                    
        }

       if(mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && radar[3][2] != 2){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x] == menor_paso){
                if(scanner[3][2] < menor_distancia){
                    movimiento = "moveS";
                    menor_distancia = scanner[3][2];
                }
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                menor_distancia = scanner[3][2];
            }   

        }

        if(mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && radar[3][3] != 2){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x+1] == menor_paso){
                if(scanner[3][3] < menor_distancia){
                    movimiento = "moveSE";
                    menor_distancia = scanner[3][3];
                }
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                menor_distancia = scanner[3][3];
            }   

        }      
       
        movimiento = comprobarObjetivoAlrededor(movimiento);
       
        return movimiento;
    }
    
    /**
    *
    * @author Alex
    */
    public void veoObjetivo(){
        for(int i = 0; i < range; i++)
            for(int j = 0; j < range; j++)
                if(radar[i][j]==3)
                    visto=true;
        if(visto)
            obtenerPosicionObjetivo();
    }
    
    /**
    *
    * @author Alex
    */
    public String busquedaAerea(){
        String movimiento = "moveN";
        int menor_paso = 50000;
        
        int n, s, e, o, ne, no, se, so;
        
        no = radar[0][0];
        n = radar[0][1];
        ne = radar[0][2];
        o = radar[1][0];
        e = radar[1][2];
        so = radar[2][0];
        s = radar[2][1];
        se = radar[2][2];
        
        
        if(no != 4 && no != 2){
            movimiento = "moveNW";
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
        }

        if(n != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && n != 2){
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
        }             

        if(ne != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && ne != 2){
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
        }

       if(o != 4 && mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && o != 2){
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
        }

        if(e != 4 && mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && e != 2){
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
        }

        if(so != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && so != 2){
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
        }
        
       if(s != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && s != 2){
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
        }

        if(se != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && se != 2){
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
        }
        
        movimiento = comprobarObjetivoAlrededor(movimiento);
        
        System.out.println(movimiento);
        
        return movimiento;
    }
    
    /**
    *
    * @author Alvaro Alex
    */
    public String comprobarObjetivoAlrededor(String ultimo_movimiento){
        String movimiento = ultimo_movimiento;
        
        int n, s, e, o, ne, no, se, so;
        
        if(tipo.equals(Tipo.COCHE)){
            no = radar[1][1];
            n = radar[1][2];
            ne = radar[1][3];
            o = radar[2][1];
            e = radar[2][3];
            so = radar[3][1];
            s = radar[3][2];
            se = radar[3][3];
        }else if(tipo.equals(Tipo.CAMION)){
            no = radar[4][4];
            n = radar[4][5];
            ne = radar[4][6];
            o = radar[5][4];
            e = radar[5][6];
            so = radar[6][4];
            s = radar[6][5];
            se = radar[6][6];
        }else{
            no = radar[0][0];
            n = radar[0][1];
            ne = radar[0][2];
            o = radar[1][0];
            e = radar[1][2];
            so = radar[2][0];
            s = radar[2][1];
            se = radar[2][2];
        }
        
        
        if(no == 3){
            movimiento = "moveNW";
        }    

        if(n == 3){
            movimiento = "moveN";
        }

        if(ne == 3){
            movimiento = "moveNE";
        }

        if(o == 3){
            movimiento = "moveW";
        }

        if(e == 3){
            movimiento = "moveE";
        }

        if(so == 3){
            movimiento = "moveSW";
        }

        if(s == 3){
            movimiento = "moveS";
        }

        if(se == 3){
            movimiento = "moveSE";
        }
        
        return movimiento;
    }
    
    /**
    *
    * @author Sergio Alex Alvaro
    * @throws org.codehaus.jettison.json.JSONException
    * @throws java.lang.InterruptedException
    */
    public void actuar() throws JSONException, InterruptedException{
        
        //Busqueda Alex
        if(inbox.getPerformativeInt()==ACLMessage.REQUEST){ 
            if(recepcion.getString("Accion").equals("Buscar")){
                obtieneMapaComun();
                if(bateria<=fuelrate){
                    
                    envio = new JSONObject();
                    envio.put("command","refuel");
                    
                    enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                    recibir_mensaje();
                    
                }else{//else Alex
                    //Pensar movimiento
                    String movimiento = "moveN";
                    if(tipo.equals(Tipo.CAMION) || tipo.equals(Tipo.COCHE)){
                        movimiento = busquedaTerrestre();
                    }else if(tipo.equals(Tipo.AEREO)){               
                        movimiento = busquedaAerea();                      
                    }
                   
                    
                    envio = new JSONObject();
                    envio.put("command",movimiento);
                    //Nos movemos
                    enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                    recibir_mensaje();
                }
                //Comrpobamos que no ha habido error al hablar con el servidor
                if(inbox.getPerformativeInt()==ACLMessage.FAILURE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD || inbox.getPerformativeInt()==ACLMessage.REFUSE){
                        finalizar =true;
                        enviar_mensaje(recepcion.getString("details"),"pizarra2",ACLMessage.REFUSE);
                }
                else{
                    //actualizamos los datos ya que nos hemos movido y se lo enviamos a pizarra2
                    envio = new JSONObject();

                    enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                    recibir_mensaje();
                    if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD)
                        enviar_mensaje(recepcion.getString("details"),"pizarra2",ACLMessage.REFUSE);
                    else
                        actualizarDatos();

                    //int map[] = new int[500000];
                    JSONArray map= new JSONArray();
                    for(int i = 0; i < 500; i++)
                        for(int j = 0; j < 500; j++)
                            map.put(mapa[i][j]);
                    CompresorArray c = new CompresorArray(map);
                    String mapaComprimido = c.getStringComprimido();
                    veoObjetivo();
                    if(visto){
                        envio.put("visto",true);
                        envio.put("o_y", objetivo_y);
                        envio.put("o_x",objetivo_x);
                    }
                    else
                        envio.put("visto",false);
                 
                    envio.put("Pasos", paso);
                    envio.put("x", gps_x);
                    envio.put("y", gps_y);
                    envio.put("MapaAux",mapaComprimido);
                    envio.put("energy", energy);
                    envio.put("Bateria", bateria);
                    
                    enviar_mensaje(envio.toString(), "pizarra2", ACLMessage.INFORM);  
                }
            }//Alvaro
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
                        enviar_mensaje(recepcion.getString("details"),"pizarra2",ACLMessage.REFUSE);
                    }
                    else{
                        
                        envio = new JSONObject();

                        enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                        recibir_mensaje();
                        if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD)
                            enviar_mensaje(recepcion.getString("details"),"pizarra2",ACLMessage.REFUSE);
                        else
                            actualizarDatos();
                    }
                }
                //Sergio
                if(goal){
                    envio = new JSONObject();
                    envio.put("EnObjetivo", true);
                    enviar_mensaje(envio.toString(),"pizarra2", ACLMessage.INFORM);                 
                }
                else{
                    if(bateria <= 1){
                        envio = new JSONObject();
                        envio.put("command","refuel");
                        
                        enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                        recibir_mensaje();
                        
                        if(inbox.getPerformativeInt()==ACLMessage.FAILURE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                            finalizar = true;
                            enviar_mensaje(recepcion.getString("details"),"pizarra2",ACLMessage.REFUSE);
                        }
                        else{
                            envio = new JSONObject();
                            
                            enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                            recibir_mensaje();
                            
                            if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                                enviar_mensaje(recepcion.getString("details"),"pizarra2",ACLMessage.REFUSE);
                            }
                            else{
                                System.out.print(recepcion.getString("Result") + ", Bateria recargada ");
                                actualizarDatos();
                            }
                            
                        }
                    }
                }
            }   
        }
            
    }

}
