package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import  java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;

public class CPU {
    //COLA de contextos
    LinkedList<Contexto> contextos;

    LinkedList<Contexto> estadisticas;
    //COLA de hilillos
    LinkedList<BufferedReader> hilillos;
    int[] RAMD;
    int[] RAMI;
    int quatum;
    private Cache cacheD0;
    private Cache cacheD1;
    private Cache cacheI0;
    private Cache cacheI1;
    public static ReentrantLock lockD = new ReentrantLock();
    public static ReentrantLock lockI = new ReentrantLock();


    private CyclicBarrier cyclicBarrier;
    private int hilos;



    Nucleo n0;
    Nucleo n1;

    public CPU(){
        quatum = 0;
        contextos = new LinkedList<>();
        estadisticas = new LinkedList<>();
        hilillos = new LinkedList<>();
        cacheD0 = new Cache('D', 8, this);
        cacheD1 = new Cache('D', 4, this);
        cacheI0 = new Cache('I', 8, this);
        cacheI1 = new Cache('I', 4, this);
        cacheD0.linkcache(cacheD1);
        cacheD1.linkcache(cacheD0);
        cacheI0.linkcache(cacheI1);
        cacheI1.linkcache(cacheI0);

        cyclicBarrier = new CyclicBarrier(1);

        n0= new Nucleo(1, cacheD0, cacheI0, this, cyclicBarrier);
        n1= new Nucleo(1, cacheD1, cacheI1, this, cyclicBarrier);

        RAMD= new int[104];
        RAMI= new int[640];
        for(int i=0; i<RAMI.length; ++i){
            RAMI[i]=63;
            RAMI[i+1]=0;
            RAMI[i+2]=0;
            RAMI[i+3]=0;
            i = i+3;
        }
        for(int i=0; i<RAMD.length; i++){
            RAMD[i]=i;
        }
    }

    private void start(int qntm) {
        quatum = qntm;
        int nHilillos = 5;
        int pcAux=0;
        for(int i=0; i<=nHilillos; i++){
            int p = pcAux;
            String line;
            try {
                BufferedReader in = new BufferedReader(new FileReader(i+".txt"));
                while (((line = in.readLine()) != null)){
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens()) {
                        RAMI[pcAux++] = Integer.parseInt(st.nextToken());
                    }
                }
                Contexto contexto= new Contexto(p,i);
                System.out.println("El hilillo "+i+" tiene un pc de "+p);
                contextos.add(contexto);
            } catch (FileNotFoundException e) {
                System.out.println("Error al leer el archivo "+i);
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error en el buffer.");
                e.printStackTrace();
            }
        }
        /*for(int kk: RAMI){
            System.out.print(kk);
        }*/
        //se carga en nucleo
        //n0.cargarHilillo(contextos.removeFirst(), 0);
        //n1.cargarHilillo(contextos.removeFirst(), 0);
        //n0.cargarHilillo(contextos.removeFirst(), 1);

        Nucleo n0 = new Nucleo(1,cacheD0, cacheI0, this, cyclicBarrier );
        Thread h0= new Thread(n0);
        Nucleo n1 = new Nucleo(1,cacheD1, cacheI1, this, cyclicBarrier );
        Thread h1= new Thread(n1);
        h0.start();
        h1.start();
        try {
            h0.join();
            h1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // D: 96 | I: 640

        //cacheD0.storeCheck(45, 97);
        //cacheD1.storeCheck(46,32);
        //cacheD0.storeCheck(31,64);
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


//Ejecuta la barrera de espera para el cpu
    public void ejecutar() {
        //try {

            //cyclicBarrier.await();// Esta es la barrera que espera a todos los hilos

        //} catch (InterruptedException ie) {
            //ie.printStackTrace();
        //} catch (BrokenBarrierException be) {
            //be.printStackTrace();
        //}
    }
}