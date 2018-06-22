package src;

import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Nucleo implements Runnable {

    CPU cpu;
    int registrosHilo0[];
    int registrosHilo1[];
    int pc[];
    int quantum[];
    int idHilillo[];// nombre del hilillo
    int id;
    int hililloP;// hilillo principal
    Cache cacheI;
    Cache cacheD;
    CyclicBarrier cyclicBarrier;

    HijoSuicida hijoSuicida;

    public Nucleo(int tipo, Cache cD,Cache  cI, CPU c, CyclicBarrier cb) {
        cacheD= cD;
        cacheI= cI;
        cpu = c;
        cyclicBarrier = cb;

        registrosHilo0 = new int[32];

        if (tipo == 0) {
            registrosHilo1 = new int[32];
            pc = new int[2];
            quantum = new int[2];
            idHilillo = new int[2];
            hijoSuicida = new HijoSuicida();
        }else {
            pc = new int[1];
            quantum = new int[1];
            idHilillo = new int[1];
        }

        id = tipo;
        hililloP = 0;
    }

    void cargarHilillo(Contexto c, int pos){
        if(pos==1) registrosHilo1= c.registros;
        else registrosHilo0= c.registros;
        pc[pos] = c.pc;
        idHilillo[pos] = c.id;
        quantum[pos] = cpu.quatum;
    }

    Contexto guardarHilillo(int pos){
        Contexto c;
        if(pos==1) c = new Contexto(registrosHilo1, pc[pos], idHilillo[pos]);
        else c = new Contexto(registrosHilo0, pc[pos], idHilillo[pos]);
        return c;
    }

    int[] fetch(int hillillo){// retorna int[] de 4
        int[] aux = new int[4];
        if (!cacheI.isInCache(pc[hillillo])) {
            falloDeCache(cacheI);
        }
        aux = cacheI.retornaIns(pc[hillillo]);
        return aux;
    }

    void ejecutarI(int[] instruccion, int iD) {

        switch (instruccion[0]) {
            //DADDI
            case 8:
                if (iD == 0) {
                    registrosHilo0[instruccion[2]] = registrosHilo0[instruccion[1]] + instruccion[3];
                } else {
                    registrosHilo1[instruccion[2]] = registrosHilo1[instruccion[1]] + instruccion[3];
                }
                break;
            //DADD
            case 32:
                if (iD == 0) {
                    registrosHilo0[instruccion[3]] = registrosHilo0[instruccion[1]] + registrosHilo0[instruccion[2]];
                } else {
                    registrosHilo1[instruccion[3]] = registrosHilo1[instruccion[1]] + registrosHilo1[instruccion[2]];
                }
                break;
            //DSUB
            case 34:
                if (iD == 0) {
                    registrosHilo0[instruccion[3]] = registrosHilo0[instruccion[1]] - registrosHilo0[instruccion[2]];
                } else {
                    registrosHilo1[instruccion[3]] = registrosHilo1[instruccion[1]] - registrosHilo1[instruccion[2]];
                }
                break;
            //DMUL
            case 12:
                if (iD == 0) {
                    registrosHilo0[instruccion[3]] = registrosHilo0[instruccion[1]] * registrosHilo0[instruccion[2]];
                } else {
                    registrosHilo1[instruccion[3]] = registrosHilo1[instruccion[1]] * registrosHilo1[instruccion[2]];
                }
                break;
            //DDIV
            case 14:
                if (iD == 0) {
                    registrosHilo0[instruccion[3]] = registrosHilo0[instruccion[1]] / registrosHilo0[instruccion[2]];
                } else {
                    registrosHilo1[instruccion[3]] = registrosHilo1[instruccion[1]] / registrosHilo1[instruccion[2]];
                }
                break;
            //BEQZ
            case 4:
                if (iD == 0) {
                    if (instruccion[1] == 0) {
                        pc[0] += 4 * instruccion[3];
                    }
                } else {
                    if (instruccion[1] == 0) {
                        pc[1] += 4 * instruccion[3];
                    }
                }
                break;
            //BNEZ
            case 5:
                if (iD != 0) {
                    if (instruccion[1] != 0) {
                        pc[0] += 4 * instruccion[3];
                    }
                } else {
                    if (instruccion[1] != 0) {
                        pc[1] += 4 * instruccion[3];
                    }
                }
                break;
            //JAL
            case 3:
                if (iD != 0) {
                    registrosHilo0[31] = instruccion[3];
                    pc[0] += instruccion[3];
                } else {
                    registrosHilo1[31] = instruccion[3];
                    pc[1] += instruccion[3];
                }
                break;
            //JR
            case 2:
                if (iD != 0) {
                    pc[0] += registrosHilo0[instruccion[1]];
                } else {
                    pc[1] += registrosHilo1[instruccion[1]];
                }
                break;
            //LW
            case 35:

                break;
            //SW
            case 43:

                break;
            //FIN
            case 63:

                break;
        }
    }

    int convertDirBloque(int dir){
        if(id == 1)return dir / 4;
        else return dir/8;
    }

    /*int convertirBloqueACache(int bloque, int palabra) {

    }*/

    public void LW(){}

    public void SW(){}

    void falloDeCache(Cache c){
        if(id==0){
            /*hijoSuicida = new HijoSuicida();
            Thread hilo= new Thread(hijoSuicida);
            hilo.start();*/
        }
        // hilo se debe matar con hilo.join() en cuanto el padre resuelva el fallo de cache?
        //R: no.
    }


    public void run(){
        while(!cpu.contextos.isEmpty()){
            while (quantum[hililloP]!= 0){
                ejecutarI(fetch(hililloP), hililloP);
                quantum[hililloP]--;
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
            guardarHilillo(hililloP);
            cargarHilillo(cpu.contextos.removeFirst(), hililloP);
            hililloP++; hililloP%=2;
        }
    }

    private class HijoSuicida implements Runnable{
        public HijoSuicida(){}
        @Override
        public void run() {
        }
    }

}
