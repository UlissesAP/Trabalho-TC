package GUI;

import GUI.enums.Operacao;

import javax.swing.*;
import java.awt.*;

public class JanelaInicial extends JDialog {

    private Operacao operacaoSelecionada;

    public JanelaInicial() {
        configurarJanela();
    }

    private void configurarJanela() {
        setTitle("Operações sobre Autômatos");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        painelPrincipal.setBackground(new Color(245, 247, 250));

        // ── Cabeçalho ──────────────────────────────────────────────────────────
        JLabel labelTitulo = new JLabel("Selecione a operação desejada", SwingConstants.CENTER);
        labelTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        labelTitulo.setForeground(new Color(33, 37, 41));
        labelTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        painelPrincipal.add(labelTitulo, BorderLayout.NORTH);

        // ── Botões de operação ─────────────────────────────────────────────────
        JPanel painelBotoes = new JPanel(new GridLayout(3, 1, 0, 12));
        painelBotoes.setOpaque(false);

        for (Operacao op : Operacao.values()) {
            JButton botao = criarBotaoOperacao(op);
            painelBotoes.add(botao);
        }

        painelPrincipal.add(painelBotoes, BorderLayout.CENTER);

        // ── Rodapé / dica ──────────────────────────────────────────────────────
        JLabel labelDica = new JLabel(
                "<html><center><i>Complemento e Estrela requerem 1 arquivo .jff<br>"
                        + "Diferença Simétrica requer 2 arquivos .jff</i></center></html>",
                SwingConstants.CENTER
        );
        labelDica.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labelDica.setForeground(new Color(108, 117, 125));
        labelDica.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        painelPrincipal.add(labelDica, BorderLayout.SOUTH);

        add(painelPrincipal);
        pack();
        centralizar();
    }

    /**
     * Cria um botão estilizado para cada operação disponível.
     */
    private JButton criarBotaoOperacao(Operacao operacao) {
        JButton botao = new JButton(operacao.getDescricao());
        botao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBackground(new Color(67, 97, 238));
        botao.setForeground(Color.WHITE);
        botao.setOpaque(true);
        botao.setBorderPainted(false);
        botao.setPreferredSize(new Dimension(260, 44));

        // Efeito de hover
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                botao.setBackground(new Color(52, 78, 210));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                botao.setBackground(new Color(67, 97, 238));
            }
        });

        // Ao clicar, armazena a operação e fecha o diálogo
        botao.addActionListener(e -> {
            operacaoSelecionada = operacao;
            dispose();
        });

        return botao;
    }

    private void centralizar() {
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (tela.width - getWidth()) / 2;
        int y = (tela.height - getHeight()) / 2;
        setLocation(x, y);
    }

    public Operacao exibir() {
        setVisible(true);   // bloqueia aqui (modal) até dispose()
        return operacaoSelecionada;
    }
}

