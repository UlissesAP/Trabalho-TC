package entities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class AutomatoFinito {
    private List<Estado> estados = new ArrayList<>();
    private List<Transicao> transicoes = new ArrayList<>();
    private Set<String> alfabeto = new HashSet<>();

    public AutomatoFinito() {
    }

    public AutomatoFinito(List<Estado> estados, List<Transicao> transicoes) {
        this.estados = estados;
        this.transicoes = transicoes;
    }

    public AutomatoFinito(AutomatoFinito a) {
        for (Estado e : a.getEstados()) {
            this.estados.add(new Estado(e.getId(), e.getNome(), e.isInicial(), e.isFinal_()));
        }

        for (Transicao t : a.getTransicoes()) {
            this.transicoes.add(new Transicao(t.getDe(), t.getPara(), t.getSimbolo()));
        }

        for (String s : a.getAlfabeto()) {
            this.alfabeto.add(s);
        }
    }

    public AutomatoFinito(File arquivo) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(arquivo);

            NodeList nos = doc.getElementsByTagName("state");

            for (int i = 0; i < nos.getLength(); i++) {
                Element elemento = (Element) nos.item(i);

                int id = Integer.parseInt(elemento.getAttribute("id"));
                String nome = elemento.getAttribute("name");
                boolean inicial = elemento.getElementsByTagName("initial").getLength() > 0;
                boolean final_ = elemento.getElementsByTagName("final").getLength() > 0;

                estados.add(new Estado(id, nome, inicial, final_));
            }

            nos = doc.getElementsByTagName("transition");

            for (int i = 0; i < nos.getLength(); i++) {
                Element elemento = (Element) nos.item(i);

                int de = Integer.parseInt(elemento.getElementsByTagName("from").item(0).getTextContent());
                int para = Integer.parseInt(elemento.getElementsByTagName("to").item(0).getTextContent());

                NodeList readNos = elemento.getElementsByTagName("read");

                String simbolo = readNos.item(0).getTextContent();
                alfabeto.add(simbolo);

                transicoes.add(new Transicao(de, para, simbolo));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao ler o arquivo .jff:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAFD() {
        for (int i = 0; i < transicoes.size(); i++) {
            Transicao transicao = transicoes.get(i);

            for (int j = i + 1; j < transicoes.size(); j++) {
                if (transicao.getDe() == transicoes.get(j).getDe() && transicao.getSimbolo().equals(transicoes.get(j).getSimbolo()))
                    return false;
            }
        }

        return true;
    }

    public boolean isCompleto() {

        for (Estado estado : estados) {

            Set<String> copiaAlfabeto = new HashSet<>(alfabeto);

            for (Transicao transicao : transicoes) {
                if (transicao.getDe() == estado.getId())
                    copiaAlfabeto.remove(transicao.getSimbolo());
            }

            if (!copiaAlfabeto.isEmpty())
                return false;

        }

        return true;
    }

    public void completarAutomato() {
        int novoId = estados.size();
        Estado estadoMorto = new Estado(novoId, "q" + novoId, false, false);

        for (Estado estado : estados) {

            Set<String> copiaAlfabeto = new HashSet<>(alfabeto);

            for (Transicao transicao : transicoes) {
                if (transicao.getDe() == estado.getId())
                    copiaAlfabeto.remove(transicao.getSimbolo());
            }

            if (!copiaAlfabeto.isEmpty()) {
                for (String simbolo : copiaAlfabeto) {
                    transicoes.add(new Transicao(estado.getId(), estadoMorto.getId(), simbolo));
                }
            }

        }

        for (String simbolo : alfabeto) {
            transicoes.add(new Transicao(estadoMorto.getId(), estadoMorto.getId(), simbolo));
        }

        estados.add(estadoMorto);
    }

    public boolean temTransicaoVazia() {
        for (Transicao transicao : transicoes) {
            if (transicao.getSimbolo() == null || transicao.getSimbolo().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int posicaoDoEstado(int id) {
        for (int i = 0; i < estados.size(); i++) {
            if (estados.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public int idDoPar(AutomatoFinito outro, int id1, int id2) {
        return posicaoDoEstado(id1) * outro.estados.size() + outro.posicaoDoEstado(id2);
    }

    public void inverterTransicoes() {
        for (Transicao transicao : transicoes) {
            int de = transicao.getDe();
            transicao.setDe(transicao.getPara());
            transicao.setPara(de);
        }
    }

    public void criarInicialComTransicoesVazias(List<Estado> destinos) {
        int novoId = 0;

        for (Estado estado : estados) {
            if (estado.getId() >= novoId) {
                novoId = estado.getId() + 1;
            }
        }

        Estado novoInicial = new Estado(novoId, "q" + novoId, true, false);
        estados.add(novoInicial);

        for (Estado destino : destinos) {
            transicoes.add(new Transicao(novoId, destino.getId(), ""));
        }
    }

    public List<Estado> getEstados() {
        return estados;
    }

    public List<Transicao> getTransicoes() {
        return transicoes;
    }

    public Set<String> getAlfabeto() {
        return alfabeto;
    }

    public Estado buscarEstadoPorId(int id) {
        for (Estado e : estados) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public int buscarDestino(int origem, String simbolo) {
        for (Transicao t : transicoes) {
            if (t.getDe() == origem && simbolo.equals(t.getSimbolo())) {
                return t.getPara();
            }
        }
        return -1;
    }

    public static long chavePar(int id1, int id2) {
        return ((long) id1 << 32) ^ (id2 & 0xFFFFFFFFL);
    }
}
