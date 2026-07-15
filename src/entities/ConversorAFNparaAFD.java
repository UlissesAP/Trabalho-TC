package entities;

import java.util.*;

public class ConversorAFNparaAFD {

    public AutomatoFinito converter(AutomatoFinito afn) {
        // AFD que sera construido
        AutomatoFinito afd = new AutomatoFinito();

        // Copia apenas os simbolos validos (ignora epsilon) para o alfabeto do AFD
        for (String s : afn.getAlfabeto()) {
            if (s != null && !s.isEmpty()) {
                afd.getAlfabeto().add(s);
            }
        }

        // Mapeia cada subconjunto de estados do AFN a um ID de estado do AFD
        Map<Set<Integer>, Integer> nomesEstados = new HashMap<>();
        // Fila de subconjuntos que ainda precisam ser processados
        Queue<Set<Integer>> filaDeProcessamento = new LinkedList<>();

        // 1. Estado inicial do AFD = fecho-epsilon do estado inicial do AFN
        Set<Integer> conjuntoInicial = new HashSet<>();
        for (Estado e : afn.getEstados()) {
            if (e.isInicial()) {
                conjuntoInicial.add(e.getId());
                break;
            }
        }
        conjuntoInicial = afn.fechoEpsilon(conjuntoInicial);

        int numeroEstado = 0;

        // Sera o estado S0 do AFD
        nomesEstados.put(conjuntoInicial, numeroEstado);

        Estado estadoS0 = new Estado(numeroEstado, "S" + numeroEstado, true, false);

        // Se o subconjunto inicial contiver algum estado final do AFN, S0 tambem sera final
        if (afn.conjuntoContemFinal(conjuntoInicial)) {
            estadoS0.setFinal_(true);
        }

        afd.getEstados().add(estadoS0);

        filaDeProcessamento.add(conjuntoInicial);
        numeroEstado++;

        // 2. Processa cada subconjunto da fila
        while (!filaDeProcessamento.isEmpty()) {
            Set<Integer> conjuntoAtual = filaDeProcessamento.poll();
            int nomeAtual = nomesEstados.get(conjuntoAtual);

            // Para cada simbolo do alfabeto, calcula o destino no AFD
            for (String simbolo : afd.getAlfabeto()) {
                // Conjunto de estados alcancaveis a partir de conjuntoAtual lendo o simbolo
                Set<Integer> conjuntoMovimento = new HashSet<>();

                for (int estado : conjuntoAtual) {
                    conjuntoMovimento.addAll(afn.moverComSimbolo(estado, simbolo));
                }

                // Aplica fecho-epsilon ao resultado do movimento
                Set<Integer> fechoEpsilon = afn.fechoEpsilon(conjuntoMovimento);

                // Se este subconjunto ainda nao existe no AFD, cria um novo estado
                if (!nomesEstados.containsKey(fechoEpsilon)) {
                    int novoId = numeroEstado++;
                    nomesEstados.put(fechoEpsilon, novoId);
                    Estado novoEstado = new Estado(novoId, "S" + novoId, false, false);

                    // Se o subconjunto contiver estado final, o novo estado tambem sera final
                    if (afn.conjuntoContemFinal(fechoEpsilon)) {
                        novoEstado.setFinal_(true);
                    }

                    afd.getEstados().add(novoEstado);
                    filaDeProcessamento.add(fechoEpsilon);
                }

                // Cria a transicao no AFD
                afd.getTransicoes().add(new Transicao(nomeAtual, nomesEstados.get(fechoEpsilon), simbolo));
            }
        }

        return afd;
    }
}
