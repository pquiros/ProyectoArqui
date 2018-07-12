package src;

import java.util.concurrent.locks.ReentrantLock;

public class Cache {

    private CPU cpu;
    private Cache othercache;
    public char estados[];
    public int etiquetas[];
    public int memoria[];
    private char tipo;
    private int blockCount;
    private int size;
    public static ReentrantLock lock = new ReentrantLock();

    public Cache(char tippo, int bloque, CPU cepeu) {


        tipo = tippo;
        cpu = cepeu;
        int tamano = 0;// tamano de bloque
        blockCount = bloque;

        switch (tipo) {
            case 'D': // cache de datos
                tamano = 4;
                estados = new char[bloque];
                etiquetas = new int[bloque];
                memoria = new int[bloque*tamano];
                break;

            case 'I': // cache de instrucciones
                tamano = 16;
                //codigo repetido ;(
                estados = new char[bloque];
                etiquetas = new int[bloque];
                memoria = new int[bloque*tamano];
                break;
        }
        size = tamano;
        for(int i=0; i<estados.length; i++){
            estados[i]='I'; etiquetas[i]=-1;
        }
    }

    public int getmemint(int pos){
        pos = pos % (blockCount * size);
        return this.memoria[pos];
    }

    public void linkcache(Cache other){
        this.othercache = other;
    }

    public int loadFromMemory(int direction){
        try {
            boolean lockAct = false;
            boolean otherLockAct = false;
            int blocks = direction / size;
            int position = blocks % blockCount;
            if(blocks == 40){
                int o = 0;
            }

            try {
                if (tipo == 'D') {
                    lockAct = cpu.lockD.tryLock();
                } else if (tipo == 'I') {
                    lockAct = cpu.lockI.tryLock();
                }

                if (!lockAct) {
                    return -1;
                } else {
                    if (estados[position] == 'M') { // Si esta en M quiere decir que es un bloque victima
                        // Reloj + 39
                        storeToMemory(direction); // Copia ese bloque a memoria y lo invalida en cache.

                        // Reloj + 1
                    }

                    // Revizar la otra cache

                    try {
                        otherLockAct = othercache.lock.tryLock(); // La posicion esta disponible en la otra cache?

                        if (!otherLockAct) { // NO
                            //no deberia retornar -1
                            // Pues nada, libera el bus en finally.
                        } else { // SI
                            if (othercache.checkCacheModified(direction) == true && othercache.checkCacheIdentity(position) == blocks) { // Esta el bloque modificado?
                                // Toma la posicion de cache y lo copia en ambos, memoria y este cache, y ambos en estado C
                                othercache.storeToMemory(direction);
                            }

                            // Carga el bloque desde memoria principal.
                            for (int i = 0; i < size; i++) {
                                if (tipo == 'D') {

                                    memoria[(position * size) + i] = cpu.RAMD[(blocks * size) + i];
                                } else if (tipo == 'I') {
                                    memoria[(position * size) + i] = cpu.RAMI[(blocks * size) + i];
                                }
                            }
                            etiquetas[position] = blocks;
                            estados[position] = 'C';
                            // Reloj +39
                        }

                    } finally {
                        if (othercache.lock.isHeldByCurrentThread())
                            othercache.lock.unlock();
                    }
                }
            } finally {
                if (cpu.lockD.isHeldByCurrentThread())
                    cpu.lockD.unlock();
                if (cpu.lockI.isHeldByCurrentThread())
                    cpu.lockI.unlock();
            }
            return 0;
        }catch (ArrayIndexOutOfBoundsException e){
            return -1;
        }
    }

    // True si es M o C

    public boolean checkCacheState(int direction) {
        boolean present = false;
        int blocks = direction / size;
        int position = blocks % blockCount;
        if((estados[position] == 'M' || estados[position] == 'C') && etiquetas[position] == blocks){
            present = true;
        }
        return present;
    }

    // True solo si es M

    public boolean checkCacheModified(int direction) {
        boolean present = false;
        int blocks = direction / size;
        int position = blocks % blockCount;
        if((estados[position] == 'M') && etiquetas[position] == blocks){
            present = true;
        }
        return present;
    }

    public int checkCacheIdentity(int direction) {
        int present = 0;
        int blocks = direction / size;
        int position = blocks % blockCount;
        present = etiquetas[position];
        return present;
    }

    public int getFromCache(int direction) {
        int blocks = direction / size;
        int position = direction % size;
        int finalep = blocks + position;
        return memoria[finalep];
    }

    public int storeCheck(int direction, int data) {
        if(tipo=='D') direction/=4;
        boolean lockAct = false;
        boolean otherLockAct = false;
        int blocks = direction / size;
        int wordp = direction % size;
        int position = blocks % blockCount;
        int success = -1;
        int count= 0;

        while(success == -1) {
            if(count > 999) {
                System.out.println("Loopiando en el store fo'eva my nigg.");
            }
            count++;
            try{
                lockAct = this.lock.tryLock();

                if(!lockAct){
                    success = -1;
                }
                else{
                    if (/*checkCacheState(direction) == true && checkCacheIdentity(direction) == blocks*/ checkCacheModified(direction)) {
                        memoria[(position*size) + wordp] = data;
                        break;
                    }else{
                        try{
                            otherLockAct = othercache.lock.tryLock(); // La posicion esta disponible en la otra cache?

                            if(!otherLockAct){ // NO
                                // Pues nada, libera el bus en finally.
                            }
                            else{ // SI
                                success = othercache.invalidate(direction);
                                if(success == 0){
                                    int otherPosition = blocks % othercache.getBlockAmount();
                                    othercache.estados[otherPosition] = 'I';
                                }
                            }
                        }finally{
                            if(othercache.lock.isHeldByCurrentThread())
                                othercache.lock.unlock();
                        }
                    }

                    if(success == 0){
                        loadFromMemory(direction);
                        memoria[(position * size) + wordp] = data;
                        estados[position] = 'M';
                    }
                }
            }finally {
                if(this.lock.isHeldByCurrentThread())
                    this.lock.unlock();
            }
        }

        return success;
    }

    public int invalidate(int direction){
        int result = 0;
        int blocks = direction / size;
        int position = blocks % blockCount;
        boolean lockAct = false;
        if((estados[position] == 'C') && etiquetas[position] == blocks){
            estados[position] = 'I';
            result = 0;
        }else if((estados[position] == 'M') && etiquetas[position] == blocks){
            result = -1;
            try{
                if(tipo == 'D'){
                    lockAct = cpu.lockD.tryLock();
                }else if(tipo == 'I'){
                    lockAct = cpu.lockI.tryLock();
                }
                result = storeToMemory(direction);
            }finally{
                if(othercache.lock.isHeldByCurrentThread())
                    othercache.lock.unlock();
                if (cpu.lockD.isHeldByCurrentThread())
                    cpu.lockD.unlock();
                if (cpu.lockI.isHeldByCurrentThread())
                    cpu.lockI.unlock();
            }
        }

        return result;
    }

    public int storeToMemory(int directon) {
        boolean lockAct = false;
        int blocks = directon / size;
        int position = blocks % blockCount;
        int block[] = new int[size];
        int vintinB = etiquetas[position];

        if(this.tipo == 'D'){
            lockAct = cpu.lockD.isHeldByCurrentThread();
        }else if(this.tipo == 'I'){
            lockAct = cpu.lockI.isHeldByCurrentThread();
        }

        if(lockAct == true){
            if(estados[position] == 'I'){
                return 0; // Esta invalido o , no hay nada que hacer.
            }else{
                for (int i = 0; i < size; i++) {
                    if (tipo == 'D') {
                        cpu.RAMD[(vintinB * size) + i] = memoria[(position * size) + i];
                    } else if (tipo == 'I') {
                        cpu.RAMI[(vintinB * size) + i] = memoria[(position * size) + i];
                    }
                }
                estados[position] = 'C'; // Se ha compartido el bloque.
                // Reloj + 1
            }
        }else{
            return -1;
        }

        return 0;
    }

    public int getBlockSize(){
        return size;
    }

    public int getMemoryData(int position){
        return memoria[position];
    }

    public int getBlockAmount(){
        return blockCount;
    }

    //necesito este metodo retorno lar instrucion
    // solo se llama si la instrución está
    int[] retornaIns(int nInstrucion){
        int[] i = new int[4];
        int pos = nInstrucion%size + size*((nInstrucion/size)%blockCount);
        for(int y = 0; y<4; y++) {
            i[y]=memoria[pos+y];
        }
        return i;
    }

    // retorna si la instruccion está en cache
    public boolean isInCache(int d) {
        return etiquetas[d/size%blockCount]== d/size;
    }
}
