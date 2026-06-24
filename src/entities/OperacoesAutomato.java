package entities;

import java.io.File;

public class OperacoesAutomato {

    public static AutomatoFinito complemento(File arquivo) {
        AutomatoFinito automato = new AutomatoFinito(arquivo);

        if (!automato.isAFD()) {
            throw new IllegalArgumentException("O autômato fornecido não é um AFD.");
        }

        for (Estado estado : automato.getEstados()) {
            estado.setFinal_(!estado.isFinal_());
        }

        if (!automato.isCompleto()) {
            automato.completarAutomato();
        }

        return automato;
    }
}
