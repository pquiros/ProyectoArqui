package src;

public class Cache {

    public CPU cpu;
    char estados[];
    int etiquetas[];
    int memoria[][];

    public Cache(char tipo, int bloque) {

        //cpu = new CPU();
        int tamano = 0;// tama;o de bloque

        switch (tipo) {
            case 'd': // cache de datos
                tamano = 4;
                estados = new char[bloque];
                etiquetas = new int[bloque];
                memoria = new int[bloque][tamano];
                break;

            case 'i': // cache de instrucciones
                tamano = 16;
                //codigo repetido ;(
                estados = new char[bloque];
                etiquetas = new int[bloque];
                memoria = new int[bloque][tamano];
                break;
        }
        for(int i=0; i<estados.length; i++){
            estados[i]='i'; etiquetas[i]=-1;
        }
    }

    public void insertFromMemory() {

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
