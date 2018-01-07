/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;

import java.util.ArrayList;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author Alex Clase para gestionar el número de veces que aparece de forma consecutiva
 */

class Apariciones{
    private int apariciones;
    private int numero;
    
    
    /**
    *
    * @author Alex
    */
    Apariciones(int n, int a){
        apariciones = a;
        numero = n;
    }
    
    /**
    *
    * @author Alex
    */
    Apariciones(int n){
        apariciones = 1;
        numero = n;
    }
    
    /**
    *
    * @author Alex
    */
    Apariciones(){
        apariciones = 0;
        numero = 0;
    }
    
    /**
    *
    * @author Alex
    */
    public void addAparicion(){
        apariciones++;
    }
    
    /**
    *
    * @author Alex
    */
    public int getApariciones(){
        return apariciones;
    }
    
    /**
    *
    * @author Alex
    */
    public int getNumero(){
        return numero;
    }
}

/**
 *
 * @author Alex Clase para comprimir un array
 */
public class CompresorArray {
    JSONArray array;
    String arrayComprimido;
    ArrayList<Apariciones> compresion;
    
    CompresorArray (JSONArray a) throws JSONException{
        array = a;
        arrayComprimido = "";
        compresion = new ArrayList();
        Comprimir();
    }
    
    CompresorArray (String s){
        arrayComprimido = s;
        array = new JSONArray();
        compresion = new ArrayList();
        Descomprimir();
    }
    
    /**
    *
    * @author Alex
    */
    public void Comprimir() throws JSONException{
        int anterior = 0;
        Apariciones a = new Apariciones();
        
        for(int i = 0; i < array.length(); i++){
            
            if(i==0){
                anterior = array.getInt(i);
                a = new Apariciones(anterior);
            }else{
                if(anterior == array.getInt(i))
                    a.addAparicion();
                else{
                    compresion.add(a);
                    anterior = array.getInt(i);
                    a = new Apariciones(anterior);
                }
                if(i == array.length()-1)
                    compresion.add(a);
            }
        }
        for(int i = 0; i < compresion.size(); i++){
            arrayComprimido += compresion.get(i).getNumero() + ":" + compresion.get(i).getApariciones();
            if(i < compresion.size()-1)
                arrayComprimido += ",";
        }
        //System.out.println(arrayComprimido);
    }
    
    /**
    *
    * @author Alex
    */
    public void Descomprimir(){
        String[] partes = arrayComprimido.split(",");
        for(int i = 0; i < partes.length; i++){
            String[] ver = partes[i].split(":");
            Apariciones a = new Apariciones(Integer.parseInt(ver[0]),Integer.parseInt(ver[1]));
            compresion.add(a);
        }
        
        for(int i = 0; i < compresion.size(); i++){
            Apariciones a = compresion.get(i);
            for(int j = 0; j < a.getApariciones(); j++)
                array.put(a.getNumero());
        }
        //System.out.println(array.toString());
    }
    
    /**
    *
    * @author Alex
    */
    public JSONArray getArraySinComprimir(){
        return array;
    }
    
    /**
    *
    * @author Alex
    */
    public String getStringComprimido(){
        return arrayComprimido;
    }
    
}