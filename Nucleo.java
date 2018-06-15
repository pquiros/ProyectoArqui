

public class Nucleo implements runnable {

    CPU cpu;
    int registrosHilo0[];
    int registrosHilo1[];
    int pc[];
    int id;
    int idHilillo;

    HijoSuicida hijoSuicida;

    public Nucleo(int tipo) {
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
    void ejecutarI(String instruccion){}
    int convertDirBloque(int dir){return 0;}
    void LW(){}
    void SW(){}
    void falloDeCache(){
        if(id==0){
            hijoSuicida.run();
        }
    }

    void run(){

    }
    private class HijoSuicida implements runnable{
        public HijoSuicida(){}
        public run(){}
    }
}
