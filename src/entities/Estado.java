package entities;

public class Estado {
    private int id;
    private String nome;
    private boolean inicial;
    private boolean final_;

    public Estado(int id, String nome, boolean inicial, boolean final_) {
        this.id = id;
        this.nome = nome;
        this.inicial = inicial;
        this.final_ = final_;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isInicial() {
        return inicial;
    }

    public void setInicial(boolean inicial) {
        this.inicial = inicial;
    }

    public boolean isFinal_() {
        return final_;
    }

    public void setFinal_(boolean final_) {
        this.final_ = final_;
    }
}
