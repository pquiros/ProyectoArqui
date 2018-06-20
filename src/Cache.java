package src;

import java.util.concurrent.locks.ReentrantLock;

public class Cache {

    private CPU cpu;
    private Cache othercache;
    private char estados[];
    private int etiquetas[];
    private int memoria[];
    private int tipo;
    private final int blockSize = 4;
    private int blockCount;
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
        for(int i=0; i<estados.length; i++){
            estados[i]='I'; etiquetas[i]=-1;
        }
    }

    public void linkcache(Cache other){
        this.othercache = other;
    }

    public int loadFromMemory(int direction){
        boolean lockAct = false;
        boolean otherLockAct = false;
        int blocks = direction / blockSize;
        int position = blocks % blockCount;

        try{
            if(tipo == 'D'){
                lockAct = cpu.lockD.tryLock();
            }else if(tipo == 'I'){
                lockAct = cpu.lockI.tryLock();
            }

            if(!lockAct) {
                return -1;
            }else {
                if(estados[position] == 'M'){ // Si esta en M quiere decir que es un bloque victima
                    // Reloj + 39
                    StoreToMemory(direction); // Copia ese bloque a memoria y lo invalida en cache.

                    // Reloj + 1
                }

                // Revizar la otra cache

                try{
                    otherLockAct = othercache.lock.tryLock(); // La posicion esta disponible en la otra cache?

                    if(!otherLockAct){ // NO
                        // Pues nada, libera el bus en finally.
                    }
                    else{ // SI
                        if (othercache.checkCacheModified(direction) == true && othercache.checkCacheIdentity(position) == blocks) { // Esta el bloque modificado?
                            // Toma la posicion de cache y lo copia en ambos, memoria y este cache, y ambos en estado C
                            othercache.StoreToMemory(direction);
                        }else{// NO
                            // Carga el bloque desde memoria principal.

                            for (int i = 0; i < 4; i++) {
                                if (tipo == 'D') {
                                    memoria[(position * blockSize) + i] = cpu.RAMD[(blocks * blockSize) + i];
                                } else if (tipo == 'I') {
                                    memoria[(position * blockSize) + i] = cpu.RAMI[(blocks * blockSize) + i];
                                }
                            }
                            etiquetas[position] = blocks;
                            estados[position] = 'C';
                        }
                    }

                }finally{
                    if(this.lock.isHeldByCurrentThread())
                        this.lock.unlock();
                }
            }
        }finally{
            if(cpu.lockD.isHeldByCurrentThread())
                cpu.lockD.unlock();
        }
        return 0;
    }

    public int loadCheck(int direction) {
        int answer = 0;
        boolean lockAct = false;
        int blocks = direction / blockSize;
        int wordp = direction % blockSize;
        int position = blocks % blockCount;
        int success = -1;

        while(success == -1) {
            try{
                lockAct = this.lock.tryLock();

                if(!lockAct){
                    success = -1;
                }
                else{
                    if (checkCacheState(direction) == true && checkCacheIdentity(position) == blocks) {
                        success = 0;
                    }else{
                        success = loadFromMemory(direction);
                    }

                    if(success == 0){
                        answer = memoria[(position*blockSize) + wordp];
                    }
                }
            }finally {
                if(this.lock.isHeldByCurrentThread())
                    this.lock.unlock();
            }
        }

        return answer;
    }

    // True si es M o C

    public boolean checkCacheState(int direction) {
        boolean present = false;
        int blocks = direction / blockSize;
        int position = blocks % blockCount;
        if((estados[position] == 'M' || estados[position] == 'C') && etiquetas[position] == blocks){
            present = true;
        }
        return present;
    }

    // True solo si es M

    public boolean checkCacheModified(int direction) {
        boolean present = false;
        int blocks = direction / blockSize;
        int position = blocks % blockCount;
        if((estados[position] == 'M') && etiquetas[position] == blocks){
            present = true;
        }
        return present;
    }

    public int checkCacheIdentity(int direction) {
        int present = 0;
        int blocks = direction / blockSize;
        int position = blocks % blockCount;
        present = etiquetas[position];
        return present;
    }

    public int getFromCache(int direction) {
        int blocks = direction / blockSize;
        int position = direction % blockSize;
        int finalep = blocks + position;
        return memoria[finalep];
    }

    public int StoreToMemory(int directon) {
        boolean lockAct = false;
        int blocks = directon / blockSize;
        int position = blocks % blockCount;
        int block[] = new int[blockSize];

        if(this.tipo == 'D'){
            lockAct = cpu.lockD.isHeldByCurrentThread();
        }else if(this.tipo == 'I'){
            lockAct = cpu.lockI.isHeldByCurrentThread();
        }

        if(lockAct == true){
            if(estados[blocks] == 'I'){
                return 0; // Esta invalido o , no hay nada que hacer.
            }else{
                for (int i = 0; i < 4; i++) {
                    if (tipo == 'D') {
                        cpu.RAMD[(blocks * blockSize) + i] = memoria[(position * blockSize) + i];
                    } else if (tipo == 'I') {
                        cpu.RAMI[(blocks * blockSize) + i] = memoria[(position * blockSize) + i];
                    }
                }
                estados[blocks] = 'C'; // Se ha compartido el bloque.
                // Reloj + 1
            }
        }else{
            return -1; // Return 'WAT?!'
        }

        return 0;
    }


    //necesito este metodo retorno la instrucion
    // solo se llama si la instrución está
    int[] retornaIns(int nInstrucion){
        int[] i = new int[4];

        return i;
    }

    // retorna si la instruccion está en cache
    public boolean isInCaheI(int nInstrucion) {
        return false;
    }
}
