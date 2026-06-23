package entities;

public class OperacoesAutomato {

    public static void complemento(AutomatoFinito automato) {
        automato.getEstados().forEach((_, estado) -> estado.setFinal_(!estado.isFinal_()));
    }
}
