

public class Nucleo implements Runnable {

    CPU cpu;
    int registrosHilo0[];
    int registrosHilo1[];
    int pc[];
    int id;
    int idHilillo;
    Cache cacheI;
    Cache cacheD;

    HijoSuicida hijoSuicida;

    public Nucleo(int tipo, Cache cD,Cache  cI) {
        cacheD= cD;
        cacheI= cI;
        registrosHilo0 = new int[32];
        if (tipo == 0) {
            registrosHilo1 = new int[32];
            pc = new int[2];
            hijoSuicida = new HijoSuicida();
        }else {
            pc = new int[1];
        }
        id = tipo;
        idHilillo = 0;
    }

    void cargarHilillo(){}
    void fetch(){}
    void ejecutarI(String instruccion){

        switch(2){}
    }
    int convertDirBloque(int dir){return 0;}
    void LW(){}
    void SW(){}
    void falloDeCache(){
        if(id==0){
            hijoSuicida = new HijoSuicida();
            Threat hilo= new Threat(hijoSuicida);
            hilo.start();
        }
    }

    void run(){

    }
    private class HijoSuicida implements Runnable{
        public HijoSuicida(){}
        public run(){}
    }
}
