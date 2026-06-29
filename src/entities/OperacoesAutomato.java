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

        // Localiza estados iniciais.
        Estado inicial1 = null;
        Estado inicial2 = null;
        for (Estado e : a1.getEstados()) {
            if (e.isInicial()) { inicial1 = e; break; }
        }
        for (Estado e : a2.getEstados()) {
            if (e.isInicial()) { inicial2 = e; break; }
        }
        if (inicial1 == null || inicial2 == null) {
            throw new IllegalArgumentException("Autômato sem estado inicial.");
        }

        AutomatoFinito resultado = new AutomatoFinito();

        // Mapa de pares (id1, id2) -> Estado do produto (somente pares alcançáveis).
        Map<Long, Estado> mapaPares = new HashMap<>();
        int proximoId = 0;

        // Cria estado inicial do produto.
        long chaveInicial = chavePar(inicial1.getId(), inicial2.getId());
        boolean finalInicial = inicial1.isFinal_() ^ inicial2.isFinal_();
        Estado estadoInicialProduto = new Estado(
                proximoId++,
                "q" + inicial1.getId() + "_" + inicial2.getId(),
                true,
                finalInicial
        );
        mapaPares.put(chaveInicial, estadoInicialProduto);
        resultado.getEstados().add(estadoInicialProduto);

        // BFS sobre os pares alcançáveis.
        Queue<int[]> fila = new LinkedList<>();
        fila.add(new int[]{inicial1.getId(), inicial2.getId()});

        while (!fila.isEmpty()) {
            int[] par = fila.poll();
            int id1 = par[0];
            int id2 = par[1];
            long chaveAtual = chavePar(id1, id2);
            Estado estadoAtual = mapaPares.get(chaveAtual);

            for (String simbolo : alfabeto) {
                int prox1 = buscarDestino(a1, id1, simbolo);
                int prox2 = buscarDestino(a2, id2, simbolo);

                // Em AFDs completos, sempre deve existir uma transição.
                if (prox1 == -1 || prox2 == -1) {
                    throw new IllegalStateException(
                            "Transição ausente em autômato que deveria estar completo.");
                }

                long chaveProx = chavePar(prox1, prox2);
                Estado estadoProx = mapaPares.get(chaveProx);

                if (estadoProx == null) {
                    Estado e1 = buscarEstadoPorId(a1, prox1);
                    Estado e2 = buscarEstadoPorId(a2, prox2);
                    boolean finalProx = e1.isFinal_() ^ e2.isFinal_();
                    estadoProx = new Estado(
                            proximoId++,
                            "q" + prox1 + "_" + prox2,
                            false,
                            finalProx
                    );
                    mapaPares.put(chaveProx, estadoProx);
                    resultado.getEstados().add(estadoProx);
                    fila.add(new int[]{prox1, prox2});
                }

                resultado.getTransicoes().add(
                        new Transicao(estadoAtual.getId(), estadoProx.getId(), simbolo)
                );
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