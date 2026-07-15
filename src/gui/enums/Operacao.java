package gui.enums;

public enum Operacao {

    COMPLEMENTO("Complemento"),
    ESTRELA("Estrela"),
    DIFERENCA_SIMETRICA("Diferença Simétrica"),
    INTERSECCAO("Intersecção"),
    REVERSO("Reverso"),
    UNIAO("União"),
    DIFERENCA("Diferença"),
    MINIMIZACAO("Minimização"),
    CONVERSAO_AFN_AFD("Conversão AFN → AFD");

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
