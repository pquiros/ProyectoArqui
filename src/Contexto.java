package src;

public class Contexto {
    int registros[];
    int pc;
    int id;
    public Contexto(int p, int i){
        registros= new int[32];
        pc=p;
        id=i;
    }
}
