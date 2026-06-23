package entities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomatoFinito {
    private Map<Integer, Estado> estados = new HashMap<>();
    private List<Transicao> transicoes = new ArrayList<>();

    public AutomatoFinito() {
    }

    public AutomatoFinito(Map<Integer, Estado> estados, List<Transicao> transicoes) {
        this.estados = estados;
        this.transicoes = transicoes;
    }

    public AutomatoFinito(String arquivo) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(arquivo));

        NodeList nos = doc.getElementsByTagName("state");

        for (int i = 0; i < nos.getLength(); i++) {
            Element elemento = (Element) nos.item(i);

            int id = Integer.parseInt(elemento.getAttribute("id"));
            String nome = elemento.getAttribute("name");
            boolean inicial = elemento.getElementsByTagName("initial").getLength() > 0;
            boolean final_ = elemento.getElementsByTagName("final").getLength() > 0;

            estados.put(id, new Estado(id, nome, inicial, final_));
        }

        nos = doc.getElementsByTagName("transition");

        for (int i = 0; i < nos.getLength(); i++) {
            Element elemento = (Element) nos.item(i);

            int de = Integer.parseInt(elemento.getElementsByTagName("from").item(0).getTextContent());
            int para = Integer.parseInt(elemento.getElementsByTagName("to").item(0).getTextContent());

            NodeList readNos = elemento.getElementsByTagName("read");

            List<String> aoLer = new ArrayList<>();

            for (int j = 0; j < readNos.getLength(); j++) {
                String simbolo = readNos.item(j).getTextContent();
                aoLer.add(simbolo);
            }

            transicoes.add(new Transicao(de, para, aoLer));
        }
    }

    public Map<Integer, Estado> getEstados() {
        return estados;
    }

    public List<Transicao> getTransicoes() {
        return transicoes;
    }
}
