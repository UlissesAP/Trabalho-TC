import gui.JanelaInicial;
import gui.SeletorArquivos;
import gui.enums.Operacao;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Program {

    private final SeletorArquivos seletorArquivos = new SeletorArquivos();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Program().iniciar());
    }

    private void iniciar() {
        configurarLookAndFeel();

        Operacao operacao = escolherOperacao();
        if (operacao == null) {
            encerrar("Nenhuma operação foi selecionada. Encerrando.");
            return;
        }

        List<File> arquivosEntrada = selecionarArquivosEntrada(operacao);
        if (arquivosEntrada == null) {
            return;
        }

        processarOperacao(operacao, arquivosEntrada);

        File destino = seletorArquivos.selecionarDestino(null);
        if (destino == null) {
            return;
        }

        salvarResultado(destino);

        JOptionPane.showMessageDialog(
                null,
                "Fluxo concluído!\n\nArquivo de destino:\n" + destino.getAbsolutePath(),
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private Operacao escolherOperacao() {
        JanelaInicial janela = new JanelaInicial();
        return janela.exibir();
    }

    private List<File> selecionarArquivosEntrada(Operacao operacao) {
        if (operacao == Operacao.DIFERENCA_SIMETRICA) {
            return seletorArquivos.selecionarDoisArquivos(null);
        } else {
            File arquivo = seletorArquivos.selecionarUmArquivo(null);
            if (arquivo == null) return null;

            List<File> lista = new ArrayList<>();
            lista.add(arquivo);
            return lista;
        }
    }

    private void processarOperacao(Operacao operacao, List<File> arquivosEntrada) {
        if (operacao == Operacao.COMPLEMENTO)

        System.out.println("[Aplicacao] processarOperacao chamado.");
        System.out.println("  Operação  : " + operacao.getDescricao());
        System.out.println("  Arquivos  : " + arquivosEntrada);
    }

    private void salvarResultado(File arquivoDestino) {

        System.out.println("[Aplicacao] salvarResultado chamado.");
        System.out.println("  Destino: " + arquivoDestino.getAbsolutePath());
        System.out.println("  (escrita do arquivo ainda não implementada)");
    }

    private void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Aplicacao] Não foi possível aplicar o L&F do sistema: " + e.getMessage());
        }
    }

    private void encerrar(String mensagem) {
        JOptionPane.showMessageDialog(
                null,
                mensagem,
                "Encerrando",
                JOptionPane.INFORMATION_MESSAGE
        );
        System.exit(0);
    }
}
