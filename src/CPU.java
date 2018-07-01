package src;

import java.io.*;
import  java.util.LinkedList;
import java.util.List;
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
    public PrintWriter writer;
    private Cache cacheD0;
    private Cache cacheD1;
    private Cache cacheI0;
    private Cache cacheI1;
    //vars de sincronia para uso de la barrera
    private boolean mode;
    private int tCiclos;
    public boolean alguienAcabaDeMorirMiAmigo;
    Thread h0;
    Thread h1;

    //buses
    public static ReentrantLock lockD = new ReentrantLock();
    public static ReentrantLock lockI = new ReentrantLock();

    public static ReentrantLock lockCola = new ReentrantLock();


    private CyclicBarrier cyclicBarrier;
    private int hilos;

    Semaphore sem = new Semaphore(1, true);

    private Nucleo n0;
    private Nucleo n1;
    public Contexto obtengaHillillo(){
        Contexto c = null;
        try{
            lockCola.lock();
            if(!contextos.isEmpty()){ c = contextos.removeFirst();}
            lockCola.unlock();
        }finally {

        }
        return c;
    }

    public CPU(){
        cyclicBarrier = new CyclicBarrier(2, new EntreB());
        quatum = 0;
        tCiclos=0;
        contextos = new LinkedList<>();
        estadisticas = new LinkedList<>();
        hilillos = new LinkedList<>();
        cacheD0 = new Cache('D', 4, this);
        cacheD1 = new Cache('D', 4, this);
        cacheI0 = new Cache('I', 4, this);
        cacheI1 = new Cache('I', 4, this);
        cacheD0.linkcache(cacheD1);
        cacheD1.linkcache(cacheD0);
        cacheI0.linkcache(cacheI1);
        cacheI1.linkcache(cacheI0);



        n0= new Nucleo(1, cacheD0, cacheI0, this, cyclicBarrier);
        n1= new Nucleo(2, cacheD1, cacheI1, this, cyclicBarrier);

        cacheD0.linkNu(n0);
        cacheD1.linkNu(n1);
        cacheI0.linkNu(n0);
        cacheI1.linkNu(n1);

        RAMD= new int[104];
        RAMI= new int[640];
        for(int i=0; i<RAMI.length; ++i){
            RAMI[i]=1;
        }
        for(int i=0; i<RAMD.length; i++){
            RAMD[i]=1;
        }
    }

    private void start(int qntm, List<Integer> nHilillos) {
        try {
            writer = new PrintWriter("the-file-name.txt");
        }catch(FileNotFoundException e){
        }
        //mode= true;
        quatum = qntm;
        //int nHilillos = 5;
        int pcAux=0;
        for(int i=0; i</*=*/nHilillos.size(); i++){
            int p = pcAux;
            String h = nHilillos.get(i).toString();
            String line;
            try {
                BufferedReader in = new BufferedReader(new FileReader(/*i*/h+".txt"));
                while (((line = in.readLine()) != null)){
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens()) {
                        RAMI[pcAux++] = Integer.parseInt(st.nextToken());
                    }
                }
                Contexto contexto= new Contexto(p,nHilillos.get(i));
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

        h0= new Thread(n0);
        h1= new Thread(n1);
        h0.start();
        h1.start();
        try {
            h0.join();
            h1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writer.close();

        // D: 96 | I: 640

        //cacheD0.storeCheck(45, 97);
        //cacheD1.storeCheck(46,32);
        //cacheD0.storeCheck(31,64);
    }
    private class EntreB implements Runnable{
        public EntreB(){}
        @Override
        public void run() {
            if(mode) if(++tCiclos%20 == 0){ try {
                //System.out.println("Espero");
                int c = System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }}
            if(alguienAcabaDeMorirMiAmigo){
                if(h0.isAlive()) n1.cyclicBarrier = new CyclicBarrier(1, new EntreB());
                if(h1.isAlive()) n0.cyclicBarrier = new CyclicBarrier(1, new EntreB());
                alguienAcabaDeMorirMiAmigo = false;
                //System.out.println("Mis condolencias");
            }

        }
    }

    public static void main(String[] args){
        String mens1 = "Introduzca el quantum que desea asignar al programa";
        String mens2 = "Debe introducir un numero";

        CPU cpu = new CPU();
        List<Integer> listaHilillos = cpu.escogerHilillos();
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
                cpu.start(qntm, listaHilillos);
            }
            catch (NumberFormatException e){
                System.out.println(mens2);
            }
        }
    }

private List escogerHilillos() {

    boolean correcto = false;
    List<Integer> listaDeHilillos = null;

    while (correcto == false) {

        System.out.println("Modo 1 lento 0 rapido.");
        Scanner scc = new Scanner(System.in);
        StringTokenizer stt = new StringTokenizer(scc.nextLine(), " ");
        if(Integer.parseInt(stt.nextToken()) == 1){mode = true;
            System.out.println("Corriendo en modo lento");}
        else  mode = false;
        System.out.println("Introduzca el número de cada hilillo que desea correr, separado por un espacio en blanco");

        Scanner sc = new Scanner(System.in);
        listaDeHilillos = new LinkedList<>();
        StringTokenizer st = new StringTokenizer(sc.nextLine(), " ");
        while(st.hasMoreTokens())
        {
            try {
                listaDeHilillos.add(Integer.parseInt(st.nextToken()));
                correcto = true;

            } catch (NumberFormatException nfe) {
                System.out.println("Debe introducir números\n");
                listaDeHilillos.clear();
                correcto = false;
            }

        }
        for (int i = 0; i < listaDeHilillos.size(); i++){
            if (listaDeHilillos.get(i) != 0 && listaDeHilillos.get(i) != 1 && listaDeHilillos.get(i) != 2 && listaDeHilillos.get(i) != 3 && listaDeHilillos.get(i) != 4 && listaDeHilillos.get(i) != 5) {
                System.out.println("Los hilillos deben estar en el rango 0-5 y separados por un espacio\n");
                correcto = false;
                listaDeHilillos.clear();
            }
        }
    };
    /*String s = "";
    for (int i = 0; i < listaDeHilillos.size(); i++){
        s += listaDeHilillos.get(i) + " ";
    }
    System.out.println("Hilillos: " + s);*/
    return listaDeHilillos;
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