package gui.enums;

public enum Operacao {

    COMPLEMENTO("Complemento"),
    ESTRELA("Estrela"),
    DIFERENCA_SIMETRICA("Diferença Simétrica"),
    INTERSECCAO("Intersecção"),
    REVERSO("Reverso");

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
