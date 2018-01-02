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
 */
public class Memoria {
    
    /**
    * @author joaquin
    */
  public void escribir(String x , String y) throws IOException{

    File archivo=new File("texto.txt");

    FileWriter escribir=new FileWriter(archivo,true);

    escribir.write(x);
    escribir.write(y);

    escribir.close();
  }
   
   /**
   * @author joaquin
   */
  public void leer(){
    
    String texto="";
    try
    {
    FileReader lector=new FileReader("texto.txt");

    BufferedReader contenido=new BufferedReader(lector);

    while((texto=contenido.readLine())!=null)
    {
    System.out.println(texto);
    }
    }
    catch(Exception e)
    {
     System.out.println("Error al leer");
    }
  }

}




