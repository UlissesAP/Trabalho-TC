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

        int idAntigoInic = -1;
        ArrayList<Integer> idAntigoFinal = new ArrayList<Integer>();

        for (Estado estado : automato.getEstados()) {
            if(estado.isInicial()){
                idAntigoInic = estado.getId();
                estado.setInicial(false);
                break;
            }
        }

        for (Estado estado : automato.getEstados()) {
            if(estado.isFinal_()){
                idAntigoFinal.add(estado.getId());
                estado.setFinal_(false);
            }
        }

        int idNovoInicio = 0;

        for (Estado estado : automato.getEstados()) {
            if (estado.getId() > idNovoInicio) {
                idNovoInicio = estado.getId();
            }
        }

        idNovoInicio = idNovoInicio + 1;
        int idNovoFinal = idNovoInicio + 1;

        Estado novoInicial = new Estado(idNovoInicio, "novoInicial", true, false);
        automato.getTransicoes().add(new Transicao(idNovoInicio, idAntigoInic, ""));
        automato.getTransicoes().add(new Transicao(idNovoInicio, idNovoFinal, ""));
        automato.getEstados().add(novoInicial);

        Estado novoFinal = new Estado(idNovoFinal , "novoFinal", false, true);//não tem transição para ninguém;
        //automato.getTransicoes().add(new Transicao(idNovoFinal, idNovoInicio, ""));
        automato.getEstados().add(novoFinal);

        for (int id : idAntigoFinal) {
            automato.getTransicoes().add(new Transicao(id, idNovoFinal, ""));
            automato.getTransicoes().add(new Transicao(id, idAntigoInic, ""));
        }

        return automato;
    }

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

        Map<Long, Estado> mapaPares = new HashMap<>();
        int proximoId = 0;

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

    public static AutomatoFinito interseccao(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);

        if (a1.temTransicaoVazia() || a2.temTransicaoVazia()) {
            throw new IllegalArgumentException("A intersecção não aceita autômatos com transições vazias.");
        }

        List<Estado> estados1 = a1.getEstados();
        List<Estado> estados2 = a2.getEstados();

        List<Estado> novosEstados = new ArrayList<>();
        List<Transicao> novasTransicoes = new ArrayList<>();

        for (int i = 0; i < estados1.size(); i++) {
            for (int j = 0; j < estados2.size(); j++) {
                Estado p = estados1.get(i);
                Estado q = estados2.get(j);

                int novoId = i * estados2.size() + j;
                String novoNome = p.getNome() + "," + q.getNome();
                boolean novoInicial = p.isInicial() && q.isInicial();
                boolean novoFinal = p.isFinal_() && q.isFinal_();

                novosEstados.add(new Estado(novoId, novoNome, novoInicial, novoFinal));
            }
        }

        for (Transicao t1 : a1.getTransicoes()) {
            for (Transicao t2 : a2.getTransicoes()) {
                if (t1.getSimbolo().equals(t2.getSimbolo())) {
                    int novoDe = a1.idDoPar(a2, t1.getDe(), t2.getDe());
                    int novoPara = a1.idDoPar(a2, t1.getPara(), t2.getPara());

                    novasTransicoes.add(new Transicao(novoDe, novoPara, t1.getSimbolo()));
                }
            }
        }

        return new AutomatoFinito(novosEstados, novasTransicoes);
    }

    public static AutomatoFinito reverso(File arquivo) {
        AutomatoFinito automato = new AutomatoFinito(arquivo);

        Estado inicialAntigo = null;
        List<Estado> finaisAntigos = new ArrayList<>();

        for (Estado estado : automato.getEstados()) {
            if (estado.isInicial()) {
                inicialAntigo = estado;
            }
            if (estado.isFinal_()) {
                finaisAntigos.add(estado);
            }
        }

        if (inicialAntigo == null) {
            throw new IllegalArgumentException("O autômato não possui estado inicial.");
        }
        if (finaisAntigos.isEmpty()) {
            throw new IllegalArgumentException("O autômato não possui estado final.");
        }

        automato.inverterTransicoes();

        for (Estado estado : automato.getEstados()) {
            estado.setInicial(false);
            estado.setFinal_(false);
        }

        inicialAntigo.setFinal_(true);

        if (finaisAntigos.size() == 1) {
            finaisAntigos.get(0).setInicial(true);
        } else {
            automato.criarInicialComTransicoesVazias(finaisAntigos);
        }

        return automato;
    }

    private static long chavePar(int id1, int id2) {
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