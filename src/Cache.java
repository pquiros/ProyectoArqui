package src;

public class Cache {

    private CPU cpu;
    private Cache othercache;
    private char estados[];
    private int etiquetas[];
    private int memoria[];
    private int tipo;

    public Cache(char tippo, int bloque, CPU cepeu) {

        tipo = tippo;
        cpu = cepeu;
        int tamano = 0;// tama;o de bloque

        switch (tipo) {
            case 'd': // cache de datos
                tamano = 4;
                estados = new char[bloque];
                etiquetas = new int[bloque];
                memoria = new int[bloque*tamano];
                break;

            case 'i': // cache de instrucciones
                tamano = 16;
                //codigo repetido ;(
                estados = new char[bloque];
                etiquetas = new int[bloque];
                memoria = new int[bloque*tamano];
                break;
        }
        for(int i=0; i<estados.length; i++){
            estados[i]='i'; etiquetas[i]=-1;
        }
    }

    public void linkcache(Cache other){
        this.othercache = other;
    }

    public int insertFromMemory(int directon) {
        boolean lockAct = false;
        try{
            lockAct = cpu.lockD.tryLock();
            if(!lockAct)
                return -1;
            // Extract from CPU
            for(int i = 0; i < 4; i++){

            }
        }finally{
            if(cpu.lockD.isHeldByCurrentThread())
                cpu.lockD.unlock();
        }
        return 0;
    }

    public boolean checkCache(int bloque) {
        return true;
    }

    public int getFromCache(int bloque, int palabra) {
        return 0;
    }

    public void loadFromMemory(int bloque) {

    }
}
