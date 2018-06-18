package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import  java.util.LinkedList;
import java.util.Scanner;

public class CPU {
    //COLA de contextos
    LinkedList<Contexto> contextos;
    //COLA de hilillos
    LinkedList<BufferedReader> hilillos;
    int[] RAMD;
    int[] RAMI;
    int quatum;
    private Cache cacheD0;
    private Cache cacheD1;
    private Cache cacheI0;
    private Cache cacheI1;
    Nucleo n0;
    Nucleo n1;

    public CPU(){
        quatum = 0;
        contextos = new LinkedList<>();
        hilillos = new LinkedList<>();
        cacheD0 = new Cache('d', 8);
        cacheD1 = new Cache('d', 4);
        cacheI0 = new Cache('i', 8);
        cacheI1 = new Cache('i', 4);
        n0= new Nucleo(0, cacheD0, cacheI0, this);
        n1= new Nucleo(1, cacheD1, cacheI1, this);

        RAMD= new int[96];
        RAMI= new int[640];
        for(int i=0; i<RAMD.length; i++){
            RAMD[i]=1;
        }
        for(int i=0; i<RAMI.length; i++){
            RAMI[i]=1;
        }
    }

    public void start(int qntm) {

        quatum = qntm;
        int nHilillos = 5;
        for(int i=0; i<=nHilillos; i++){
            try {
                BufferedReader in = new BufferedReader(new FileReader(i+".txt"));
                hilillos.add(in);
            } catch (FileNotFoundException e) {
                System.out.println("Error al leer el archivo "+i);
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args){
        String mens1 = "Introduzca el quantum que desea asignar al programa";
        String mens2 = "Debe introducir un numero";

        CPU cpu = new CPU();
        int qntm = 0;
        String s;
        boolean isNumber = false;
        System.out.println(mens1);

        while(qntm < 1 && !isNumber){
            Scanner scan = new Scanner(System.in);
            s = scan.next();

            try {
                qntm = Integer.parseInt(s);
                isNumber = true;
                cpu.start(qntm);
            }
            catch (NumberFormatException e){
                System.out.println(mens2);
            }
        }
    }
}
