package src;

import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Nucleo implements Runnable {

    CPU cpu;
    int registrosHilo0[];
    int registrosHilo1[];
    int pc[];
    boolean needContext[];
    int quantum[];
    int idHilillo[];// nombre del hilillo
    int id;
    int hililloP;// hilillo principal
    Cache cacheI;
    Cache cacheD;
    CyclicBarrier cyclicBarrier;
    int endAmount = 0;
    HijoSuicida hijoSuicida;
    int tipoNucleo;

    public Nucleo(int tipo, Cache cD,Cache  cI, CPU c, CyclicBarrier cb) {
        cacheD= cD;
        cacheI= cI;
        cpu = c;
        cyclicBarrier = cb;
        tipoNucleo = tipo;
        registrosHilo0 = new int[32];

        if (tipo == 0) {
            registrosHilo1 = new int[32];
            pc = new int[2];
            needContext = new boolean[2];
            needContext[0] = true;
            needContext[1] = true;
            quantum = new int[2];
            idHilillo = new int[2];
            hijoSuicida = new HijoSuicida();
        }else {
            pc = new int[1];
            needContext = new boolean[1];
            needContext[0] = true;
            quantum = new int[1];
            idHilillo = new int[1];
        }

        id = tipo;
        hililloP = 0;
    }

    void cargarHilillo(Contexto c, int pos){
        needContext[pos] = false;
        if(pos==1) registrosHilo1= c.registros;
        else registrosHilo0= c.registros;
        pc[pos] = c.pc;
        idHilillo[pos] = c.id;
        quantum[pos] = cpu.quatum;
    }

    Contexto guardarHilillo(int pos){
        Contexto c;
        if(pos==1) {
            c = new Contexto(registrosHilo1, pc[pos], idHilillo[pos]);
        } else {
            c = new Contexto(registrosHilo0, pc[pos], idHilillo[pos]);
        }
        return c;
    }

    int[] fetch(int hillillo){// retorna int[] de 4
        int[] aux;
        if (!cacheI.isInCache(pc[hillillo])) {
            falloDeCache(cacheI, pc[hillillo], hillillo);
        }
        aux = cacheI.retornaIns(pc[hillillo]);
        return aux;
    }

    boolean ejecutarI(int[] instruccion, int iD) {
        try {
            cpu.sem.acquire();
            System.out.print(" --- Hilo " + this.tipoNucleo + " empieza ---\n");
            System.out.print("[" + instruccion[0] + " " + instruccion[1] + " " + instruccion[2] + " " + instruccion[3] + "]\n");
            cpu.sem.release();
        } catch(final InterruptedException ie) {
            System.out.print("Oh no");
        }
        switch (instruccion[0]) {
            //DADDI
            case 8:
                if (hililloP == 0) {
                    int par1 = registrosHilo0[instruccion[1]];
                    int par2 = instruccion[3];
                    registrosHilo0[instruccion[2]] = par1 + par2;
                } else {
                    int par1 = registrosHilo1[instruccion[1]];
                    int par2 = instruccion[3];
                    registrosHilo1[instruccion[2]] = par1 + par2;
                }
                break;
            //DADD
            case 32:
                if (hililloP == 0) {
                    int par1 = registrosHilo0[instruccion[1]];
                    int par2 = registrosHilo0[instruccion[2]];
                    registrosHilo0[instruccion[3]] = par1 + par2;
                } else {
                    int par1 = registrosHilo1[instruccion[1]];
                    int par2 = registrosHilo1[instruccion[2]];
                    registrosHilo1[instruccion[3]] = par1 + par2;
                }
                break;
            //DSUB
            case 34:
                if (hililloP == 0) {
                    int par1 = registrosHilo0[instruccion[1]];
                    int par2 = registrosHilo0[instruccion[2]];
                    registrosHilo0[instruccion[3]] = par1 - par2;
                } else {
                    int par1 = registrosHilo1[instruccion[1]];
                    int par2 = registrosHilo1[instruccion[2]];
                    registrosHilo1[instruccion[3]] = par1 - par2;
                }
                break;
            //DMUL
            case 12:
                if (hililloP == 0) {
                    int par1 = registrosHilo0[instruccion[1]];
                    int par2 = registrosHilo0[instruccion[2]];
                    registrosHilo0[instruccion[3]] = par1 * par2;
                } else {
                    int par1 = registrosHilo1[instruccion[1]];
                    int par2 = registrosHilo1[instruccion[2]];
                    registrosHilo1[instruccion[3]] = par1 * par2;
                }
                break;
            //DDIV
            case 14:
                if (hililloP == 0) {
                    int par1 = registrosHilo0[instruccion[1]];
                    int par2 = registrosHilo0[instruccion[2]];
                    registrosHilo0[instruccion[3]] = par1 / par2;
                } else {
                    int par1 = registrosHilo1[instruccion[1]];
                    int par2 = registrosHilo1[instruccion[2]];
                    registrosHilo1[instruccion[3]] = par1 / par2;
                }
                break;
            //BEQZ
            case 4:
                if (hililloP == 0) {
                    if (registrosHilo0[instruccion[1]] == 0) {
                        pc[hililloP] += 4 * instruccion[3];
                    }else{
                        return false;
                    }
                } else {
                    if (registrosHilo1[instruccion[1]] == 0) {
                        pc[hililloP] += 4 * instruccion[3];
                    }else{
                        return false;
                    }
                }
                break;
            //BNEZ
            case 5:
                if (hililloP == 0) {
                    if (registrosHilo0[instruccion[1]] != 0) { // Cambio
                        pc[hililloP] += 4 * instruccion[3];
                    }else{
                        return false;
                    }
                } else {
                    if (registrosHilo1[instruccion[1]] != 0) { // Cambio
                        pc[hililloP] += 4 * instruccion[3];
                    }else{
                        return false;
                    }
                }
                break;
            //JAL
            case 3:
                if (hililloP == 0) {
                    registrosHilo0[31] = pc[0];
                    pc[0] = pc[0] + instruccion[3];
                } else {
                    registrosHilo1[31] = pc[1];
                    pc[1] = pc[1] + instruccion[3];
                }
                break;
            //JR
            case 2:
                if (hililloP == 0) {
                    pc[0] = registrosHilo0[instruccion[1]];
                } else {
                    pc[1] = registrosHilo1[instruccion[1]];
                }
                break;
            //LW
            case 35:
                if (hililloP == 0) {
                    int direccion = registrosHilo0[instruccion[1]]+instruccion[3];
                    registrosHilo0[instruccion[2]]= LW(direccion);
                } else {
                    int direccion = registrosHilo1[instruccion[1]]+instruccion[3];
                    registrosHilo1[instruccion[2]]= LW(direccion);
                }
                break;
            //SW
            case 43:
                if (hililloP == 0) {
                    int direccion = registrosHilo0[instruccion[1]]+instruccion[3];
                    cacheD.storeCheck(direccion, registrosHilo0[instruccion[2]]);
                } else {
                    int direccion = registrosHilo1[instruccion[1]]+instruccion[3];
                    cacheD.storeCheck(direccion, registrosHilo1[instruccion[2]]);
                }
                break;
            //FIN
            case 63:
                ++endAmount;
                try {
                    cpu.sem.acquire();
                    cpu.writer.println(idHilillo[hililloP] + " : \n");
                    for (int n = 0; n < 32; n++) {
                        cpu.writer.println(n + " " + registrosHilo0[n]);
                    }
                    cpu.sem.release();
                } catch(final InterruptedException ie) {
                    System.out.print("Oh no");
                }
                cpu.estadisticas.addLast(guardarHilillo(hililloP));
                needContext[hililloP] = true;
                if(id==0)hililloP++; hililloP%=2;
                return true;
            default:
                System.out.println("Error instruccion no reconociada!!!");
        }
        try {
            cpu.sem.acquire();
            System.out.print(" --- Hilo " + this.tipoNucleo + " termino | Quantum: " + this.quantum[hililloP] + " ---\n");
            System.out.print("[" + instruccion[0] + " " + instruccion[1] + " " + instruccion[2] + " " + instruccion[3] + "]\n");
            if (instruccion[0] == 35 || instruccion[0] == 43) {
                System.out.print("Cache " + (instruccion[3] / 4) + " = " + this.cacheD.getmemint(instruccion[3] / 4) + "\n");
            } else {
                System.out.print("\t\t\t\t\t\t\t\t\tRegistro " + instruccion[2] + " = " + this.registrosHilo0[instruccion[2]] + "\n");
            }
            cpu.sem.release();
        } catch(final InterruptedException ie) {
            System.out.print("Oh no");
        }
        return false;
    }

    int convertDirBloque(int dir){
        if(id == 1)return dir / 4;
        else return dir/8;
    }

    /*int convertirBloqueACache(int bloque, int palabra) {

    }*/

    public int LW(int direction){
        try {
            direction /= 4;
            int answer = 0;
            boolean lockAct = false;
            int blocks = direction / cacheD.getBlockSize();
            int wordp = direction % cacheD.getBlockSize();
            int position = blocks % cacheD.getBlockAmount();
            int success = -1;
            int count = 0;

            while (success == -1) {
                    count++;
                if(count > 999) {
                    System.out.println("Loopiando en el loud fo'eva my nigg.");
                }
                try {
                    lockAct = cacheD.lock.tryLock();

                    if (!lockAct) {
                        success = -1;
                    } else {
                        if (cacheD.checkCacheState(direction) == true && cacheD.checkCacheIdentity(direction) == blocks) {
                            success = 0;
                        } else {
                            success = cacheD.loadFromMemory(direction);
                        }

                        if (success == 0) {
                            answer = cacheD.getMemoryData((position * cacheD.getBlockSize()) + wordp);
                        }
                    }
                } finally {
                    if (cacheD.lock.isHeldByCurrentThread())
                        cacheD.lock.unlock();
                }
            }

            return answer;
        }catch (NullPointerException e){
            return -1;
        }
    }

    public void SW(){}

    void falloDeCache(Cache c, int d, int hillillo){
        if(id==0){
            /*hijoSuicida = new HijoSuicida();
            Thread hilo= new Thread(hijoSuicida);
            hilo.start();*/
        }
        while (!cacheI.isInCache(pc[hillillo])) {
            if(80<d){
                int o = 0;
            }
            cacheI.loadFromMemory(d);
        }

        // hilo se debe matar con hilo.join() en cuanto el padre resuelva el fallo de cache?
        //R: no.
    }


    public void run(){ // MAX 376
        Contexto k;
        while((k=cpu.obtengaHillillo()) != null){

            cargarHilillo(k, hililloP);

            if(id==0)hililloP++; hililloP%=2;
            boolean var;
            while (quantum[hililloP]!= 0){
                int[] instruccion = fetch(hililloP);
                pc[hililloP] += 4;
                if(idHilillo[hililloP] == 5 &&  instruccion[0] == 2){
                    int o = 0;
                }
                var = ejecutarI(instruccion, hililloP);
                if(instruccion[0] == 43 && instruccion[1] == 16 && instruccion[2] == 2 && instruccion[3] == 0) {
                    int o = 0;
                }
                if (var) {break;}
                quantum[hililloP]--;
                /*
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }*/
            }
            if(quantum[hililloP] < 1) {
                cpu.contextos.addLast(guardarHilillo(hililloP));
            }
        }
        cpu.alguienAcabaDeMorirMiAmigo = true;
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private class HijoSuicida implements Runnable{
        public HijoSuicida(){}
        @Override
        public void run() {
        }
    }

}
