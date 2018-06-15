public class Cache {

    public CPU cpu;
    char estados[];
    int etiquetas[];
    int memoria[];

    public Cache(char tipo) {

        cpu = new CPU();
        int tamano = 0;

        switch (tipo) {
            case 'd': // cache de datos
                tamano = 16;
                estados = new char[tamano];
                etiquetas = new int[tamano];
                memoria = new int[tamano];
                break;

            case 'i': // cache de instrucciones
                tamano = 32;
                estados = new char[tamano];
                etiquetas = new int[tamano];
                memoria = new int[tamano];
        }
    }

    public void insertFromMemory() {

    }

    public boolean checkCache(int bloque) {
        return;
    }

    public getFromCache(int bloque, int palabra) {

    }

    public void loadFromMemory(int bloque) {

    }
}
