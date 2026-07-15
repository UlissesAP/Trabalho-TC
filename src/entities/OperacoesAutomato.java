package entities;

import java.io.File;
import java.util.*;

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

    public static AutomatoFinito estrela(File arquivo){
        AutomatoFinito automato = new AutomatoFinito(arquivo);

        if (!automato.isAFD()) {
            throw new IllegalArgumentException("O autômato fornecido não é um AFD.");
        }

        if (!automato.isCompleto()) {
            automato.completarAutomato();
        }

        int idAntigoInic = -1;

        for (Estado estado : automato.getEstados()) {
            if(estado.isInicial()){
                idAntigoInic = estado.getId();
                estado.setInicial(false);
                break;
            }
        }

        int idNovo = 0;

        for (Estado estado : automato.getEstados()) {
            idNovo++;
        }

        Estado novoInicial = new Estado(idNovo, "novoInicial", true, true);
        automato.getTransicoes().add(new Transicao(idNovo, idAntigoInic, ""));
        automato.getEstados().add(novoInicial);

        for (Estado estado : automato.getEstados()) {

            if(estado.isFinal_()) {
                automato.getTransicoes().add(new Transicao(estado.getId(), idAntigoInic, ""));
            }
        }

        return automato;
    }

    /**
     * Diferença simétrica entre dois AFDs: L(A1) △ L(A2) = (L(A1) \ L(A2)) ∪ (L(A2) \ L(A1)).
     * Implementada via construção de produto, marcando como finais
     * os pares (q1, q2) em que exatamente um dos dois estados é final (XOR).
     */
    public static AutomatoFinito diferencaSimetrica(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);

        if (!a1.isAFD() || !a2.isAFD()) {
            throw new IllegalArgumentException("Os autômatos fornecidos devem ser AFDs.");
        }

        if (!a1.isCompleto()) {
            a1.completarAutomato();
        }
        if (!a2.isCompleto()) {
            a2.completarAutomato();
        }

        // Alfabeto: união dos alfabetos dos dois autômatos.
        Set<String> alfabeto = new LinkedHashSet<>();
        for (Transicao t : a1.getTransicoes()) {
            if (t.getSimbolo() != null && !t.getSimbolo().isEmpty()) {
                alfabeto.add(t.getSimbolo());
            }
        }
        for (Transicao t : a2.getTransicoes()) {
            if (t.getSimbolo() != null && !t.getSimbolo().isEmpty()) {
                alfabeto.add(t.getSimbolo());
            }
        }

        AutomatoFinito resultado = new AutomatoFinito();

        int n2 = a2.getEstados().size();

        // Cria um estado no produto para cada par (e1, e2).
        for (Estado e1 : a1.getEstados()) {
            for (Estado e2 : a2.getEstados()) {
                int novoId = e1.getId() * n2 + e2.getId();
                boolean inicial = e1.isInicial() && e2.isInicial();
                boolean final_ = e1.isFinal_() ^ e2.isFinal_();

                Estado novoEstado = new Estado(
                        novoId,
                        "q" + e1.getId() + "_" + e2.getId(),
                        inicial,
                        final_
                );

                resultado.getEstados().add(novoEstado);
            }
        }

        // Cria as transições do produto: para cada par e cada símbolo do alfabeto.
        for (Estado e1 : a1.getEstados()) {
            for (Estado e2 : a2.getEstados()) {
                int idOrigem = e1.getId() * n2 + e2.getId();

                for (String simbolo : alfabeto) {
                    int destino1 = buscarDestino(a1, e1.getId(), simbolo);
                    int destino2 = buscarDestino(a2, e2.getId(), simbolo);

                    // Em AFDs completos, sempre deve existir uma transição.
                    if (destino1 == -1 || destino2 == -1) {
                        throw new IllegalStateException(
                                "Transição ausente em autômato que deveria estar completo.");
                    }

                    int idDestino = destino1 * n2 + destino2;

                    resultado.getTransicoes().add(
                            new Transicao(idOrigem, idDestino, simbolo)
                    );
                }
            }
        }

        return resultado;
    }

    private static long chavePar(int id1, int id2) {
        // Combina dois inteiros em uma chave long única.
        return ((long) id1 << 32) ^ (id2 & 0xFFFFFFFFL);
    }

    private static Estado buscarEstadoPorId(AutomatoFinito automato, int id) {
        for (Estado e : automato.getEstados()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    private static int buscarDestino(AutomatoFinito automato, int origem, String simbolo) {
        for (Transicao t : automato.getTransicoes()) {
            if (t.getDe() == origem && simbolo.equals(t.getSimbolo())) {
                return t.getPara();
            }
        }
        return -1;
    }

}