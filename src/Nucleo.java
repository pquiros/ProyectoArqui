package src;

import java.lang.Thread;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Runnable;

public class Nucleo implements Runnable {

    CPU cpu;
    int registrosHilo0[];
    int registrosHilo1[];
    int pc[];
    int id;
    int idHilillo;
    Cache cacheI;
    Cache cacheD;

    HijoSuicida hijoSuicida;

    public Nucleo(int tipo, Cache cD,Cache  cI, CPU c) {
        cacheD= cD;
        cacheI= cI;
        cpu = c;
        registrosHilo0 = new int[32];

        if (tipo == 0) {
            registrosHilo1 = new int[32];
            pc = new int[2];
            hijoSuicida = new HijoSuicida();
        }else {
            pc = new int[1];
        }

        id = tipo;
        idHilillo = 0;
    }

    void cargarHilillo(){}
    void fetch(){}
    void ejecutarI(String instruccion){

        switch(2){}
    }
    int convertDirBloque(int dir){return 0;}
    void LW(){}
    void SW(){}
    void falloDeCache(){
        if(id==0){
            hijoSuicida = new HijoSuicida();
            Thread hilo= new Thread(hijoSuicida);
            hilo.start();
        }
    }

    public void run(){

    }
    private class HijoSuicida implements Runnable{
        public HijoSuicida(){}
        @Override
        public void run() {

        }
    }


    public void leerArchivo() {

        String file = "RUTA DEL ARCHIVO";
        BufferedReader br = null;
        String line = "";
        String separador = " ";

        try {

            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] linea = line.split(separador);

                System.out.println("Codigo Operacion : " + linea[0] + " , registroD=" + linea[1] + " , registrof=" + linea[2] + " , inmediato=" + linea[3]);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
