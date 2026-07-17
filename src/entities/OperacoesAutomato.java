package entities;

import java.io.File;
import java.util.*;

public class OperacoesAutomato {

    public static AutomatoFinito complemento(File arquivo) {
        AutomatoFinito automato = new AutomatoFinito(arquivo);

        if (!automato.isAFD()) {
            automato = new ConversorAFNparaAFD().converter(automato);
        }

        if (!automato.isCompleto()) {
            automato.completarAutomato();
        }

        for (Estado estado : automato.getEstados()) {
            estado.setFinal_(!estado.isFinal_());
        }

        return automato;
    }

    public static AutomatoFinito complemento(AutomatoFinito a) {
        AutomatoFinito automato = new AutomatoFinito(a);

        if (!automato.isAFD()) {
            automato = new ConversorAFNparaAFD().converter(automato);
        }

        if (!automato.isCompleto()) {
            automato.completarAutomato();
        }

        for (Estado estado : automato.getEstados()) {
            estado.setFinal_(!estado.isFinal_());
        }

        return automato;
    }

public static AutomatoFinito estrela(File arquivo){
    AutomatoFinito automato = new AutomatoFinito(arquivo);

    int idAntigoInic = -1;
    ArrayList<Integer> idAntigoFinal = new ArrayList<>();

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

    if (idAntigoInic == -1) {
        throw new IllegalArgumentException("Autômato sem estado inicial.");
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
    automato.getEstados().add(novoInicial);

    Estado novoFinal = new Estado(idNovoFinal , "novoFinal", false, true);
    automato.getEstados().add(novoFinal);

    automato.getTransicoes().add(new Transicao(idNovoInicio, idAntigoInic, ""));
    automato.getTransicoes().add(new Transicao(idNovoInicio, idNovoFinal, ""));

    for (int id : idAntigoFinal) {
        automato.getTransicoes().add(new Transicao(id, idNovoFinal, ""));
        automato.getTransicoes().add(new Transicao(id, idAntigoInic, ""));
    }

    return automato;
}

    public static AutomatoFinito diferencaSimetrica(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);

        AutomatoFinito diferenca1 = diferenca(a1, a2);
        AutomatoFinito diferenca2 = diferenca(a2, a1);

        AutomatoFinito automatoFinal = uniao(diferenca1, diferenca2);

        return automatoFinal;
    }

    public static AutomatoFinito interseccao(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);

        if (!a1.isAFD()) {
            a1 = new ConversorAFNparaAFD().converter(a1);
        }
        if (!a2.isAFD()) {
            a2 = new ConversorAFNparaAFD().converter(a2);
        }

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

    public static AutomatoFinito interseccao(AutomatoFinito automato1, AutomatoFinito automato2) {
        AutomatoFinito a1 = new AutomatoFinito(automato1);
        AutomatoFinito a2 = new AutomatoFinito(automato2);

        if (!a1.isAFD()) {
            a1 = new ConversorAFNparaAFD().converter(a1);
        }
        if (!a2.isAFD()) {
            a2 = new ConversorAFNparaAFD().converter(a2);
        }

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

    /*==================================
    ========= GRUPO 1 ==================
    ===================================*/

    public static AutomatoFinito uniao(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);

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

        int idInicial2Original = inicial2.getId();

        int maxId1 = 0;
        for (Estado e : a1.getEstados()) {
            if (e.getId() > maxId1) maxId1 = e.getId();
        }

        int offset = maxId1 + 1;
        Map<Integer, Integer> idMap = new HashMap<>();
        for (Estado e : a2.getEstados()) {
            int antigo = e.getId();
            int novo = antigo + offset;
            idMap.put(antigo, novo);
            e.setId(novo);
            e.setNome(e.getNome() + "_2");
        }
        for (Transicao t : a2.getTransicoes()) {
            t.setDe(idMap.get(t.getDe()));
            t.setPara(idMap.get(t.getPara()));
        }

        int idNovoInicial = 0;
        for (Estado e : a1.getEstados()) {
            if (e.getId() >= idNovoInicial) idNovoInicial = e.getId() + 1;
        }
        for (Estado e : a2.getEstados()) {
            if (e.getId() >= idNovoInicial) idNovoInicial = e.getId() + 1;
        }

        Estado novoInicial = new Estado(idNovoInicial, "qNovoInicial", true, false);

        List<Transicao> todasTransicoes = new ArrayList<>(a1.getTransicoes());
        todasTransicoes.addAll(a2.getTransicoes());
        todasTransicoes.add(new Transicao(idNovoInicial, inicial1.getId(), ""));
        todasTransicoes.add(new Transicao(idNovoInicial, idMap.get(idInicial2Original), ""));

        List<Estado> todosEstados = new ArrayList<>(a1.getEstados());
        todosEstados.addAll(a2.getEstados());
        todosEstados.add(novoInicial);

        return new AutomatoFinito(todosEstados, todasTransicoes);
    }

    public static AutomatoFinito uniao(AutomatoFinito automato1, AutomatoFinito automato2) {
        AutomatoFinito a1 = new AutomatoFinito(automato1);
        AutomatoFinito a2 = new AutomatoFinito(automato2);

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

        int idInicial2Original = inicial2.getId();

        int maxId1 = 0;
        for (Estado e : a1.getEstados()) {
            if (e.getId() > maxId1) maxId1 = e.getId();
        }

        int offset = maxId1 + 1;
        Map<Integer, Integer> idMap = new HashMap<>();
        for (Estado e : a2.getEstados()) {
            int antigo = e.getId();
            int novo = antigo + offset;
            idMap.put(antigo, novo);
            e.setId(novo);
            e.setNome(e.getNome() + "_2");
        }
        for (Transicao t : a2.getTransicoes()) {
            t.setDe(idMap.get(t.getDe()));
            t.setPara(idMap.get(t.getPara()));
        }

        int idNovoInicial = 0;
        for (Estado e : a1.getEstados()) {
            if (e.getId() >= idNovoInicial) idNovoInicial = e.getId() + 1;
        }
        for (Estado e : a2.getEstados()) {
            if (e.getId() >= idNovoInicial) idNovoInicial = e.getId() + 1;
        }

        Estado novoInicial = new Estado(idNovoInicial, "qNovoInicial", true, false);

        List<Transicao> todasTransicoes = new ArrayList<>(a1.getTransicoes());
        todasTransicoes.addAll(a2.getTransicoes());
        todasTransicoes.add(new Transicao(idNovoInicial, inicial1.getId(), ""));
        todasTransicoes.add(new Transicao(idNovoInicial, idMap.get(idInicial2Original), ""));

        List<Estado> todosEstados = new ArrayList<>(a1.getEstados());
        todosEstados.addAll(a2.getEstados());
        todosEstados.add(novoInicial);

        return new AutomatoFinito(todosEstados, todasTransicoes);
    }

    public static AutomatoFinito diferenca(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);
        if (!a1.isAFD()) {
            a1 = new ConversorAFNparaAFD().converter(a1);
        }
        if (!a2.isAFD()) {
            a2 = new ConversorAFNparaAFD().converter(a2);
        }
        if(!a1.isCompleto()) {
            a1.completarAutomato();
        }
        if(!a2.isCompleto()) {
            a2.completarAutomato();
        }
        AutomatoFinito complementado = complemento(a2);
        return interseccao(a1, complementado);
    }

    public static AutomatoFinito diferenca(AutomatoFinito automato1, AutomatoFinito automato2) {
        AutomatoFinito a1 = new AutomatoFinito(automato1);
        AutomatoFinito a2 = new AutomatoFinito(automato2);
        if (!a1.isAFD()) {
            a1 = new ConversorAFNparaAFD().converter(a1);
        }
        if (!a2.isAFD()) {
            a2 = new ConversorAFNparaAFD().converter(a2);
        }
        if(!a1.isCompleto()) {
            a1.completarAutomato();
        }
        if(!a2.isCompleto()) {
            a2.completarAutomato();
        }
        AutomatoFinito complementado = complemento(a2);
        return interseccao(a1, complementado);
    }

    /*======================================================*/

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

    public static AutomatoFinito minimizar(File arquivo) {
        AutomatoFinito automato = new AutomatoFinito(arquivo);
        return new Minimizador().minimizar(automato);
    }

    public static AutomatoFinito converterAFNParaAFD(File arquivo) {
        AutomatoFinito automato = new AutomatoFinito(arquivo);
        return new ConversorAFNparaAFD().converter(automato);
    }

    public static AutomatoFinito concatenar(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);
        AutomatoFinito a2 = new AutomatoFinito(arquivo2);
        return new Concatenador().concatenar(a1, a2);
    }

    public static AutomatoFinito concatenar(AutomatoFinito a1, AutomatoFinito a2) {
        return new Concatenador().concatenar(a1, a2);
    }

    public static AutomatoFinito homomorfismo(File arquivo, Map<String, String> mapeamento) {
        AutomatoFinito automato = new AutomatoFinito(arquivo);
        return new Homomorfismo().aplicar(automato, mapeamento);
    }

    public static AutomatoFinito homomorfismo(AutomatoFinito automato, Map<String, String> mapeamento) {
        return new Homomorfismo().aplicar(automato, mapeamento);
    }
}