/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica_3_dba;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/**
 *
 * @author joaqu
 */
public class Memoria {
    /*Clase que permite escribir en un archivo de texto*/

//Importamos clases que se usaran
public void escribir(String x , String y) throws IOException{

//Un texto cualquiera guardado en una variabl
//Crear un objeto File se encarga de crear o abrir acceso a un archivo que se especifica en su constructor
File archivo=new File("texto.txt");
//Crear objeto FileWriter que sera el que nos ayude a escribir sobre archivo
FileWriter escribir=new FileWriter(archivo,true);
//Escribimos en el archivo con el metodo write 
escribir.write(x);
escribir.write(y);
//Cerramos la conexion
escribir.close();
}

}


