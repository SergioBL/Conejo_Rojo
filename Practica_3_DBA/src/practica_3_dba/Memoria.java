/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
/**
 * @author joaquin
 * Esto es una clase auxiliar donde almacenamos donde está el objetivo para en otras ejecuciones probar
 * lo que es mandar a los vehiculos directamente al objetivo
 */
public class Memoria {
    private int x;
    private int y;
    /**
    * @author Joaquin
    */
  public void escribir(String x , String y) throws IOException{
      System.out.println("Escribiendo txt en memoria");
    this.x=Integer.parseInt(x);
    this.y=Integer.parseInt(y);
    
    File archivo=new File("texto.txt");

    FileWriter escribir=new FileWriter(archivo,true);

    escribir.write(x);
    escribir.append("\r\n");
    escribir.write(y);

    escribir.close();
  }
   
   /**
   * @author Joaquin
   */
  public boolean leer(){
       System.out.println("entrando en leer");
    boolean existe=true;
    int a = 0;
    String texto="";
    try
    {
    FileReader lector=new FileReader("texto.txt");

    BufferedReader contenido=new BufferedReader(lector);
 
    while((texto=contenido.readLine())!=null)
    {
        if(a==0){
          this.x=Integer.parseInt(texto);
          a=1;
        }
        this.y=Integer.parseInt(texto);
        System.out.println(texto);
    }
    }
    catch(Exception e)
    {
     System.out.println("Error al leer");
     existe=false;
    }
    return existe;
  }
  
  public int getX(){
  return x;
  }
  
  public int getY(){
  return y;
  }

}




