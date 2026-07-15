package entities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
        todasTransicoes.add(new Transicao(idNovoInicial, idMap.get(inicial2.getId()), ""));

        List<Estado> todosEstados = new ArrayList<>(a1.getEstados());
        todosEstados.addAll(a2.getEstados());
        todosEstados.add(novoInicial);

        return new AutomatoFinito(todosEstados, todasTransicoes);
    }

    public static AutomatoFinito diferenca(File arquivo1, File arquivo2) {
        AutomatoFinito a1 = new AutomatoFinito(arquivo1);

        if (!a1.isAFD()) {
            throw new IllegalArgumentException("O primeiro autômato não é um AFD.");
        }

        AutomatoFinito complementado = complemento(arquivo2);

        File temp;
        try {
            temp = File.createTempFile("diferenca_", ".jff");
            temp.deleteOnExit();
            salvarEmArquivo(complementado, temp);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criar arquivo temporário: " + e.getMessage());
        }

        return interseccao(arquivo1, temp);
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

    private static void salvarEmArquivo(AutomatoFinito automato, File arquivo) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            Element estrutura = doc.createElement("structure");
            doc.appendChild(estrutura);

            Element tipo = doc.createElement("type");
            tipo.setTextContent("fa");
            estrutura.appendChild(tipo);

            Element automatoEl = doc.createElement("automaton");

            for (Estado e : automato.getEstados()) {
                Element estado = doc.createElement("state");
                estado.setAttribute("id", String.valueOf(e.getId()));
                estado.setAttribute("name", e.getNome());

                Element x = doc.createElement("x");
                x.setTextContent(String.valueOf(100 * (e.getId() + 1)));
                estado.appendChild(x);

                Element y = doc.createElement("y");
                y.setTextContent(String.valueOf(200));
                estado.appendChild(y);

                if (e.isInicial()) {
                    estado.appendChild(doc.createElement("initial"));
                }
                if (e.isFinal_()) {
                    estado.appendChild(doc.createElement("final"));
                }

                automatoEl.appendChild(estado);
            }

            for (Transicao t : automato.getTransicoes()) {
                Element transicao = doc.createElement("transition");

                Element de = doc.createElement("from");
                de.setTextContent(String.valueOf(t.getDe()));
                transicao.appendChild(de);

                Element para = doc.createElement("to");
                para.setTextContent(String.valueOf(t.getPara()));
                transicao.appendChild(para);

                Element simbolo = doc.createElement("read");
                simbolo.setTextContent(t.getSimbolo());
                transicao.appendChild(simbolo);

                automatoEl.appendChild(transicao);
            }

            estrutura.appendChild(automatoEl);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(arquivo));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao salvar arquivo temporário: " + e.getMessage());
        }
    }
}