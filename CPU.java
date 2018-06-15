import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import  java.util.LinkedList;

public class CPU {
    //COLA de contextos
    LinkedList<Contexto> contextos;
    //COLA de hilillos
    LinkedList<BufferedReader> hilillos;
    int[] RAMD;
    int[] RAMI;
    int quatum;
    Cache cacheD0;
    Cache cacheD1;
    Cache cacheI0;
    Cache cacheI1;
    Nucleo n0;
    Nucleo n1;


    public void start(String [] args) {
        quatum = Integer.parseInt(args[0]);
        int nHilillos = 0;
        for(int i=0; i<nHilillos; i++){
            try {
                BufferedReader in = new BufferedReader(new FileReader(i+".txt"));
                hilillos.add(in);
            } catch (FileNotFoundException e) {
                System.out.println("Error al leer el archivo "+i);
                e.printStackTrace();
            }
        }

        //n0= new Nucleo();
    }

    public static void main(String [] args){
        CPU cpu = new CPU();
        cpu.start(args);
    }
}
