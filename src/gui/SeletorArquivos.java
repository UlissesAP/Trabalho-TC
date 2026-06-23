package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SeletorArquivos {

    private static final FileNameExtensionFilter FILTRO_JFF =
            new FileNameExtensionFilter("Arquivos JFLAP (*.jff)", "jff");

    public File selecionarUmArquivo(Component pai) {
        JFileChooser seletor = criarSeletorBase();
        seletor.setDialogTitle("Selecione o arquivo .jff de entrada");
        seletor.setMultiSelectionEnabled(false);

        int resultado = seletor.showOpenDialog(pai);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            return seletor.getSelectedFile();
        }

        mostrarMensagemCancelamento(pai, "Nenhum arquivo foi selecionado.");
        return null;
    }

    public List<File> selecionarDoisArquivos(Component pai) {
        List<File> arquivos = new ArrayList<>();

        // ── Primeiro arquivo ─────────────────────────────────────────────
        JFileChooser seletor1 = criarSeletorBase();
        seletor1.setDialogTitle("Diferença Simétrica – 1º arquivo .jff");
        seletor1.setMultiSelectionEnabled(false);

        int r1 = seletor1.showOpenDialog(pai);
        if (r1 != JFileChooser.APPROVE_OPTION) {
            mostrarMensagemCancelamento(pai, "Seleção do 1º arquivo cancelada.");
            return null;
        }
        arquivos.add(seletor1.getSelectedFile());

        // ── Segundo arquivo ──────────────────────────────────────────────
        JFileChooser seletor2 = criarSeletorBase();
        seletor2.setDialogTitle("Diferença Simétrica – 2º arquivo .jff");
        seletor2.setMultiSelectionEnabled(false);
        seletor2.setCurrentDirectory(arquivos.get(0).getParentFile());

        int r2 = seletor2.showOpenDialog(pai);
        if (r2 != JFileChooser.APPROVE_OPTION) {
            mostrarMensagemCancelamento(pai, "Seleção do 2º arquivo cancelada.");
            return null;
        }
        arquivos.add(seletor2.getSelectedFile());

        return arquivos;
    }

    public File selecionarDestino(Component pai) {
        JFileChooser seletor = criarSeletorBase();
        seletor.setDialogTitle("Salvar resultado como...");
        seletor.setSelectedFile(new File("resultado.jff"));

        int resultado = seletor.showSaveDialog(pai);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File destino = seletor.getSelectedFile();

            // Garante que a extensão .jff esteja presente
            if (!destino.getName().toLowerCase().endsWith(".jff")) {
                destino = new File(destino.getAbsolutePath() + ".jff");
            }

            // Confirmação de sobrescrita, caso o arquivo já exista
            if (destino.exists()) {
                int confirmacao = JOptionPane.showConfirmDialog(
                        pai,
                        "O arquivo \"" + destino.getName() + "\" já existe.\nDeseja substituí-lo?",
                        "Confirmar substituição",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirmacao != JOptionPane.YES_OPTION) {
                    return null;
                }
            }

            return destino;
        }

        mostrarMensagemCancelamento(pai, "Nenhum destino de salvamento foi escolhido.");
        return null;
    }

    private JFileChooser criarSeletorBase() {
        JFileChooser seletor = new JFileChooser(
                System.getProperty("user.home")
        );
        seletor.setFileFilter(FILTRO_JFF);
        seletor.setAcceptAllFileFilterUsed(false);
        return seletor;
    }

    private void mostrarMensagemCancelamento(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(
                pai,
                mensagem,
                "Operação cancelada",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}

