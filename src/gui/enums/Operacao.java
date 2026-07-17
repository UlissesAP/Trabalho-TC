package gui.enums;

public enum Operacao {

    UNIAO("União"),
    DIFERENCA("Diferença"),
    INTERSECCAO("Intersecção"),
    REVERSO("Reverso"),
    CONCATENACAO("Concatenação"),
    HOMOMORFISMO("Homomorfismo"),
    ESTRELA("Estrela"),
    CONVERSAO_AFN_AFD("Conversão AFN → AFD"),
    COMPLEMENTO("Complemento"),
    DIFERENCA_SIMETRICA("Diferença Simétrica"),
    MINIMIZACAO("Minimização");

    private final String descricao;

    Operacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
