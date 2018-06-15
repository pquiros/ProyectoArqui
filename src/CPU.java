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
    Cache cacheD0;
    Cache cacheD1;
    Cache cacheI0;
    Cache cacheI1;
    Nucleo n0;
    Nucleo n1;


    public void start(int qntm) {
        quatum = qntm;
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

    public static void main(String[] args){
        String mens1 = "Introdusca el quantum que desea asignar al programa";
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
