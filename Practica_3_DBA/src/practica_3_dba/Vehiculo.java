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
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
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
    private MyDrawPanel m;
    private JFrame jframe;
   
    
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
        this.nombre = aid.name;
        inicializarMapa();
    }
    
    /**
    *
    * @author Alex  Joaquin
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
            envio = new JSONObject();
            envio.put("details : "+this.nombre,recepcion.getString("details"));
             enviar_mensaje(envio.toString(), "pizarra", ACLMessage.REFUSE);
            return false;
        }//En caso contrario, la conexión tiene exito y enviamos a pizarra quien somos y que somos
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
                enviar_mensaje(" ", "pizarra", ACLMessage.REFUSE);
                return false;
            }else
                actualizarDatos();
            
            envio = new JSONObject();
            envio.put("TipoVehiculo",tipo.toString());
            envio.put("ID",nombreVehiculo);
            envio.put("x", gps_x);
            envio.put("y", gps_y);
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
                if(mapa[i][j]==-1){
                    objetivo_x = j;
                    objetivo_y = i;
                }
            }}
    
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
                else if(radar[i][j]==3)
                   mapa[500/2 + gps_y - medio + i][500/2 + gps_x - medio + j] = -1;
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
    
    /**
    *
    * @author Alex Alvaro
    */
    public void obtieneScanner() throws JSONException{
        scanner = new int[500][500];
        System.out.println("Vehiculo  -  ObtieneScanner");
        JSONArray scan = recepcion.getJSONArray("Scanner");
        System.out.println("Vehiculo  -  ObtieneScanner2");
        for(int i = 0; i < scan.length(); i+=500)
            for(int j = 0; j < 500; j++)
                scanner[i/500][j] = scan.getInt(i+j);
        System.out.println("Vehiculo  -  ObtieneScanner3");
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
            System.out.println("Vehiculo " + getAid() + " muerto");
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
        
        
        int rn, rs, re, rw, rne, rnw, rse, rsw;
        int sn, ss, se, sw, sne, snw, sse, ssw;
        
        if(tipo.equals(Tipo.COCHE)){
            rnw = radar[1][1];
            rn = radar[1][2];
            rne = radar[1][3];
            rw = radar[2][1];
            re = radar[2][3];
            rsw = radar[3][1];
            rs = radar[3][2];
            rse = radar[3][3];
        }else{
            rnw = radar[4][4];
            rn = radar[4][5];
            rne = radar[4][6];
            rw = radar[5][4];
            re = radar[5][6];
            rsw = radar[6][4];
            rs = radar[6][5];
            rse = radar[6][6];
        }
        
        snw = scanner[500/2 + gps_y-1][500/2 + gps_x-1];
        sn = scanner[500/2 + gps_y-1][500/2 + gps_x];
        sne = scanner[500/2 + gps_y-1][500/2 + gps_x+1];
        sw = scanner[500/2 + gps_y][500/2 + gps_x-1];
        se = scanner[500/2 + gps_y][500/2 + gps_x+1];
        ssw = scanner[500/2 + gps_y+1][500/2 + gps_x-1];
        ss = scanner[500/2 + gps_y+1][500/2 + gps_x];
        sse = scanner[500/2 + gps_y+1][500/2 + gps_x+1];
        
        
        if(rnw != 1 && rnw != 4 && rnw != 2){
            movimiento = "moveNW";                      
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
            menor_distancia = snw;
        }

        if(rn != 1 && mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && rn != 2 && rn != 4){
            if(mapa[500/2 + gps_y-1][500/2 + gps_x] == menor_paso){
                if(sn < menor_distancia){
                    movimiento = "moveN";
                    menor_distancia = sn;
                }
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                menor_distancia = sn;
            }  

        }             

        if(rne != 1 && mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && rne != 2 && rne != 4){
            if(mapa[500/2 + gps_y-1][500/2 + gps_x+1] == menor_paso){
                if(sne < menor_distancia){
                    movimiento = "moveNE";
                    menor_distancia = sne;
                }
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                menor_distancia = sne;
            }    

        }

       if(rw != 1 && mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && rw != 2 && rw != 4){
            if(mapa[500/2 + gps_y][500/2 + gps_x-1] == menor_paso){
                if(sw < menor_distancia){
                    movimiento = "moveW";
                    menor_distancia = sw;
                }
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                menor_distancia = sw;
            }  

        }

        if(re != 1 && mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && re != 2 && re != 4){
            if(mapa[500/2 + gps_y][500/2 + gps_x+1] == menor_paso){
                if(se < menor_distancia){
                    movimiento = "moveE";         
                    menor_distancia = se;
                }
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                menor_distancia = se;
            }   

        }

        if(rsw != 1 && mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && rsw != 2 && rsw != 4){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x-1] == menor_paso){
                if(ssw < menor_distancia){
                    movimiento = "moveSW";  
                    menor_distancia = ssw;
                }
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                menor_distancia = ssw;
            }                    
        }

       if(rs != 1 && mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && rs != 2 && rs != 4){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x] == menor_paso){
                if(ss < menor_distancia){
                    movimiento = "moveS";
                    menor_distancia = ss;
                }
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                menor_distancia = ss;
            }   

        }

        if(rse != 1 && mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && rse != 2 && rse != 4){
            if(mapa[500/2 + gps_y+1][500/2 + gps_x+1] == menor_paso){
                if(sse < menor_distancia){
                    movimiento = "moveSE";
                    menor_distancia = sse;
                }
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                menor_distancia = sse;
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
        ArrayList<String> movimientos = new ArrayList();
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
        
        Random random = new Random();
        
        if(no != 1 && no != 4 && no != 2){
            movimiento = "moveNW";                      
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
            movimientos.add(movimiento);
        }

        if(n != 1 && n != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && n != 2){
            if(menor_paso == mapa[500/2 + gps_y-1][500/2 + gps_x]){
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                movimientos.add(movimiento);
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }             

        if(ne != 1 && ne != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && ne != 2){
            if(menor_paso == mapa[500/2 + gps_y-1][500/2 + gps_x+1]){
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                movimientos.add(movimiento);
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }

       if(o != 1 && o != 4 && mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && o != 2){
           if(menor_paso == mapa[500/2 + gps_y][500/2 + gps_x-1]){
                    movimiento = "moveW";
                    menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                movimientos.clear();
                movimientos.add(movimiento);
           }
        }

        if(e != 1 && e != 4 && mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && e != 2){
            if(menor_paso == mapa[500/2 + gps_y][500/2 + gps_x+1]){
                    movimiento = "moveE";
                    menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }

        if(so != 1 && so != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && so != 2){
            if(menor_paso == mapa[500/2 + gps_y+1][500/2 + gps_x-1]){
                    movimiento = "moveSW";
                    menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }
        
       if(s != 1 && s != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && s != 2){
           if(menor_paso == mapa[500/2 + gps_y+1][500/2 + gps_x]){
                    movimiento = "moveS";
                    menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                movimientos.clear();
                movimientos.add(movimiento);
           }
        }

        if(se != 1 && se != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && se != 2){
            if(menor_paso == mapa[500/2 + gps_y+1][500/2 + gps_x+1]){
                    movimiento = "moveSE";
                    menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }
        
        movimiento = movimientos.get(random.nextInt(movimientos.size()));
        
        movimiento = comprobarObjetivoAlrededor(movimiento);
        
        return movimiento;
    }
    
    /**
    *
    * @author Alvaro
    */
    public String movimientoAereoConScanner(){
        String movimiento = "";
        int menor_distancia = 25001;
        ArrayList<String> movimientos = new ArrayList();
        
        Random random = new Random();
        
        int rn, rs, re, rw, rne, rnw, rse, rsw;
        int sn, ss, se, sw, sne, snw, sse, ssw;
        
        rnw = radar[0][0];
        rn = radar[0][1];
        rne = radar[0][2];
        rw = radar[1][0];
        re = radar[1][2];
        rsw = radar[2][0];
        rs = radar[2][1];
        rse = radar[2][2];
        
        snw = scanner[500/2 + gps_y-1][500/2 + gps_x-1];
        sn = scanner[500/2 + gps_y-1][500/2 + gps_x];
        sne = scanner[500/2 + gps_y-1][500/2 + gps_x+1];
        sw = scanner[500/2 + gps_y][500/2 + gps_x-1];
        se = scanner[500/2 + gps_y][500/2 + gps_x+1];
        ssw = scanner[500/2 + gps_y+1][500/2 + gps_x-1];
        ss = scanner[500/2 + gps_y+1][500/2 + gps_x];
        sse = scanner[500/2 + gps_y+1][500/2 + gps_x+1];
        
        
        if(rnw != 2 && rnw != 4){
            movimiento = "moveNW";  
            movimientos.add(movimiento);
            menor_distancia = snw; 
        }
                              
        if(sn <= menor_distancia && rn != 2 && rn != 4){
            movimiento = "moveN";
            if(sn < menor_distancia){
                menor_distancia = sn;
                movimientos.clear();
            }         
            movimientos.add(movimiento);
                                         
        }             

        if(sne <= menor_distancia && rne != 2 && rne != 4){         
            movimiento = "moveNE";
            if(sne < menor_distancia){
                menor_distancia = sne;
                movimientos.clear();
            }
            movimientos.add(movimiento);
        }

       if(sw <= menor_distancia && rw != 2 && rw != 4){
            movimiento = "moveW";
            if(sw < menor_distancia){
                menor_distancia = sw;
                movimientos.clear();
            }
            
            movimientos.add(movimiento);
        }

        if(se <= menor_distancia && re != 2 && re != 4){ 
            movimiento = "moveE";
            if(se < menor_distancia){
                menor_distancia = se;
                movimientos.clear();
            }
            
            movimientos.add(movimiento);
           }

        if(ssw <= menor_distancia && rsw != 2 && rsw != 4){
            movimiento = "moveSW";
            if(ssw < menor_distancia){
                menor_distancia = ssw;    
                movimientos.clear();    
            }
            movimientos.add(movimiento);
        }

       if(ss <= menor_distancia && rs != 2 && rs != 4){
            movimiento = "moveS";
            if(ss < menor_distancia){
                menor_distancia = ss;
                movimientos.clear();
            }
            movimientos.add(movimiento);
        }

        if(sse <= menor_distancia && rse != 2 && rse != 4){   
            movimiento = "moveSE";
            if(sse < menor_distancia){
                menor_distancia = sse;
                movimientos.clear();
            }
            movimientos.add(movimiento);
        }      
       
        
        movimiento = movimientos.get(random.nextInt(movimientos.size()));
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
        ArrayList<String> movimientos = new ArrayList();
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
        
        
        Random random = new Random();
        
        if(no != 4 && no != 2){
            movimiento = "moveNW";                      
            menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x-1];
            movimientos.add(movimiento);
        }

        if(n != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x] <= menor_paso && n != 2){
            if(menor_paso == mapa[500/2 + gps_y-1][500/2 + gps_x]){
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                movimientos.add(movimiento);
            }
            else{
                movimiento = "moveN";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }             

        if(ne != 4 && mapa[500/2 + gps_y-1][500/2 + gps_x+1] <= menor_paso && ne != 2){
            if(menor_paso == mapa[500/2 + gps_y-1][500/2 + gps_x+1]){
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                movimientos.add(movimiento);
            }
            else{
                movimiento = "moveNE";
                menor_paso = mapa[500/2 + gps_y-1][500/2 + gps_x+1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }

       if(o != 4 && mapa[500/2 + gps_y][500/2 + gps_x-1] <= menor_paso && o != 2){
           if(menor_paso == mapa[500/2 + gps_y][500/2 + gps_x-1]){
                    movimiento = "moveW";
                    menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveW";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x-1];
                movimientos.clear();
                movimientos.add(movimiento);
           }
        }

        if(e != 4 && mapa[500/2 + gps_y][500/2 + gps_x+1] <= menor_paso && e != 2){
            if(menor_paso == mapa[500/2 + gps_y][500/2 + gps_x+1]){
                    movimiento = "moveE";
                    menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveE";
                menor_paso = mapa[500/2 + gps_y][500/2 + gps_x+1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }

        if(so != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x-1] <= menor_paso && so != 2){
            if(menor_paso == mapa[500/2 + gps_y+1][500/2 + gps_x-1]){
                    movimiento = "moveSW";
                    menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveSW";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x-1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }
        
       if(s != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x] <= menor_paso && s != 2){
           if(menor_paso == mapa[500/2 + gps_y+1][500/2 + gps_x]){
                    movimiento = "moveS";
                    menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveS";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x];
                movimientos.clear();
                movimientos.add(movimiento);
           }
        }

        if(se != 4 && mapa[500/2 + gps_y+1][500/2 + gps_x+1] <= menor_paso && se != 2){
            if(menor_paso == mapa[500/2 + gps_y+1][500/2 + gps_x+1]){
                    movimiento = "moveSE";
                    menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                    movimientos.add(movimiento);
            }
            else{
                movimiento = "moveSE";
                menor_paso = mapa[500/2 + gps_y+1][500/2 + gps_x+1];
                movimientos.clear();
                movimientos.add(movimiento);
            }
        }
        
        movimiento = movimientos.get(random.nextInt(movimientos.size()));
        
        movimiento = comprobarObjetivoAlrededor(movimiento);
        
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
        
        
        if(no == 3 && no != 4){
            movimiento = "moveNW";
        }    

        if(n == 3 && n != 4){
            movimiento = "moveN";
        }

        if(ne == 3 && ne != 4){
            movimiento = "moveNE";
        }

        if(o == 3 && o != 4){
            movimiento = "moveW";
        }

        if(e == 3 && e != 4){
            movimiento = "moveE";
        }

        if(so == 3 && so != 4){
            movimiento = "moveSW";
        }

        if(s == 3 && s != 4){
            movimiento = "moveS";
        }

        if(se == 3 && se != 4){
            movimiento = "moveSE";
        }
        
        return movimiento;
    }
    
    /**
    *
    * @author Sergio Alex Alvaro Joaquin
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
                        envio = new JSONObject();
                        envio.put("details : "+this.nombre,recepcion.getString("details"));
                        enviar_mensaje(envio.toString(),"pizarra",ACLMessage.REFUSE);
                }
                else{
                    //actualizamos los datos ya que nos hemos movido y se lo enviamos a pizarra
                    envio = new JSONObject();

                    enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                    recibir_mensaje();
                    if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                        enviar_mensaje(recepcion.toString(),"pizarra",ACLMessage.REFUSE);
                    }else
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
                    
                    enviar_mensaje(envio.toString(), "pizarra", ACLMessage.INFORM);  
                }
            }//Alvaro
            else if(recepcion.getString("Accion").equals("LlegaObjetivo")){
                if(scanner == null){
                    paso = 0;
                    obtieneScanner();
                    inicializarMapa();
                    jframe = new JFrame();
                    m = new MyDrawPanel(mapa,true);
                    jframe.add(m);
                    jframe.setSize(mapa.length, mapa.length);
                    jframe.setVisible(true);
                    jframe.setTitle("Gugel2");
                }
                
                while(!goal || finalizar){ 
                    if(bateria <= fuelrate){
                        envio = new JSONObject();
                        envio.put("command","refuel");
                        
                        enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                        recibir_mensaje();
                        
                        if(inbox.getPerformativeInt()==ACLMessage.REFUSE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                            if(recepcion.getString("details").equals("BAD ENERGY")){
                                System.out.println("No hay mas recargas disponibles,Energia Agotada");
                                finalizar = true;
                            }
                            finalizar = true;
                            envio = new JSONObject();
                            envio.put("details : "+this.nombre,recepcion.getString("details"));
                            enviar_mensaje(envio.toString(),"pizarra",ACLMessage.REFUSE);
                            
                        }
                        else{
                            envio = new JSONObject();
                            
                            enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                            recibir_mensaje();
                            
                            if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                                envio = new JSONObject();
                                envio.put("details : "+this.nombre,recepcion.getString("details"));
                                enviar_mensaje(envio.toString(),"pizarra",ACLMessage.REFUSE);
                            }
                            else{                               
                                actualizarDatos();
                            }
                            
                        }
                    }
                    else{                                         
                        String movimiento = "";               

                        if(tipo.equals(Tipo.CAMION) || tipo.equals(Tipo.COCHE)){
                            movimiento = movimientoTerrestreConScanner();
                        }else if(tipo.equals(Tipo.AEREO)){               
                            movimiento = movimientoAereoConScanner();                      
                        }                                   

                        envio = new JSONObject();
                        envio.put("command",movimiento);
                        enviar_mensaje(envio.toString(),"Achernar",ACLMessage.REQUEST);
                        recibir_mensaje();   

                        if(inbox.getPerformativeInt()==ACLMessage.FAILURE || inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD || inbox.getPerformativeInt()==ACLMessage.REFUSE){
                            finalizar =true;
                            envio = new JSONObject();
                            envio.put("details : "+this.nombre,recepcion.getString("details"));
                            enviar_mensaje(envio.toString(),"pizarra",ACLMessage.REFUSE);
                        }else{   
                            envio = new JSONObject();

                            enviar_mensaje("","Achernar",ACLMessage.QUERY_REF);
                            recibir_mensaje();
                            if(inbox.getPerformativeInt()==ACLMessage.NOT_UNDERSTOOD){
                                envio = new JSONObject();
                                envio.put("details : "+this.nombre,recepcion.getString("details"));
                                enviar_mensaje(envio.toString(),"pizarra",ACLMessage.REFUSE);
                            }else
                                actualizarDatos();
                        }
                    }
                    m.Update(mapa);
                    m.repaint();
                }
                
                if(goal){
                    finalizar = true;
                    envio = new JSONObject();
                    envio.put("EnObjetivo", true);
                    jframe.dispose();
                    enviar_mensaje(envio.toString(),"pizarra", ACLMessage.INFORM);                  
                }               
            }   
        }
            
    }

}
