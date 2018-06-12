public class Nucleo {

    CPU cpu;
    int registrosHilo0[];
    int registrosHilo1[];
    int pc[];
    int id;
    int idHilillo;

    public Nucleo(int tipo) {
        registrosHilo0 = new int[32];

        if (tipo == 0) {
            registrosHilo1 = new int[32];
            pc = new int[2];
        }else {
            pc = new int[1];
        }

        id = tipo;
        idHilillo = 0;

    }
}
