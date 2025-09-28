import ListaLigada.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
/**
 * Classe que representa uma interface gráfica para simulação visual
 * do Problema de Josephus. Permite entrada dos parâmetros n, k e delay
 * e mostra a eliminação visual das pessoas até restar um sobrevivente.
 */
public class JosephusGUI extends JFrame {

    private JTextField campoN, campoK, campoDelay;
    private JPanel painelPessoas;
    private JButton botaoResolver, botaoReiniciar, botaoSair;
    private ArrayList<JPanel> listaPaineisPessoas;

    public JosephusGUI() {
        setTitle("Problema de Josephus - Visual");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel de entrada com n, k e intervalo
        JPanel painelEntrada = new JPanel(new GridLayout(3, 2, 10, 10));
        painelEntrada.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelEntrada.add(new JLabel("Número de pessoas (n):"));
        campoN = new JTextField("7");
        painelEntrada.add(campoN);
        painelEntrada.add(new JLabel("Passos de contagem (k):"));
        campoK = new JTextField("3");
        painelEntrada.add(campoK);
        painelEntrada.add(new JLabel("Intervalo (ms):"));
        campoDelay = new JTextField("500");
        painelEntrada.add(campoDelay);
        add(painelEntrada, BorderLayout.NORTH);

        // Atualiza visual assim que n for alterado
        campoN.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarPessoas(); }
            public void removeUpdate(DocumentEvent e) { atualizarPessoas(); }
            public void changedUpdate(DocumentEvent e) { atualizarPessoas(); }
        });

        // Painel visual das pessoas
        painelPessoas = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelPessoas.setBackground(Color.WHITE);
        add(new JScrollPane(painelPessoas), BorderLayout.CENTER);

        // Botões de controle
        botaoResolver = new JButton("Executar Josephus");
        botaoResolver.addActionListener(this::executarJosephus);
        botaoReiniciar = new JButton("Reiniciar");
        botaoReiniciar.addActionListener(this::reiniciar);
        botaoSair = new JButton("Sair");
        botaoSair.addActionListener(e -> System.exit(0));

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoes.add(botaoResolver);
        painelBotoes.add(botaoReiniciar);
        painelBotoes.add(botaoSair);
        add(painelBotoes, BorderLayout.SOUTH);

        // Estado inicial
        atualizarPessoas();

        setVisible(true);
    }

    /**
     * Reinicia a interface para o estado inicial, permitindo nova entrada de dados
     * e recriando a visualização das pessoas.
     * 
     * @param e Evento de clique no botão "Reiniciar".
     */
    private void reiniciar(ActionEvent e) {
        campoN.setEnabled(true);
        campoK.setEnabled(true);
        campoDelay.setEnabled(true);
        botaoResolver.setEnabled(true);
        atualizarPessoas();
    }
    
    /**
     * Atualiza a visualização das pessoas com base no valor atual de n.
     * Caso n seja inválido, a função simplesmente ignora até entrada válida.
     */
    private void atualizarPessoas() {
        try {
            int n = Integer.parseInt(campoN.getText().trim());
            if (n > 0) criarPessoasVisuais(n);
        } catch (NumberFormatException ex) {
            // ignora até digitar valor válido
        }
    }
    
    /**
     * Cria visualmente os painéis que representam as pessoas com base no valor de n.
     * Cada pessoa é representada por um painel verde numerado.
     * 
     * @param n Número de pessoas a serem criadas na visualização.
     */
    private void criarPessoasVisuais(int n) {
        painelPessoas.removeAll();
        listaPaineisPessoas = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            JPanel pessoa = new JPanel();
            pessoa.setPreferredSize(new Dimension(50, 50));
            pessoa.setBackground(Color.GREEN);
            pessoa.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JLabel label = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            pessoa.add(label);
            listaPaineisPessoas.add(pessoa);
            painelPessoas.add(pessoa);
        }
        painelPessoas.revalidate();
        painelPessoas.repaint();
    }
    
    /**
     * Inicia a execução visual da simulação do problema de Josephus.
     * Valida as entradas, desativa os campos durante a execução e executa
     * o algoritmo de eliminação em uma thread separada (SwingWorker).
     * 
     * @param e Evento de clique no botão "Executar Josephus".
     */
    private void executarJosephus(ActionEvent e) {
        try {
            int n = Integer.parseInt(campoN.getText().trim());
            int k = Integer.parseInt(campoK.getText().trim());
            int delay = Integer.parseInt(campoDelay.getText().trim());
            if (n <= 0 || k <= 0 || delay < 0) {
                JOptionPane.showMessageDialog(this, "n e k devem ser positivos e intervalo >= 0.");
                return;
            }

            // Desabilita controles durante execução
            campoN.setEnabled(false);
            campoK.setEnabled(false);
            campoDelay.setEnabled(false);
            botaoResolver.setEnabled(false);

            final ListaDuplamenteLigadaCircular listaRef = new ListaDuplamenteLigadaCircular();
            for (int i = 1; i <= n; i++) listaRef.inserirFim(i);

            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                
                protected Void doInBackground() throws Exception {
                    No atual = listaRef.getInicio();
                    while (listaRef.getQtdNos() > 1) {
                        for (int i = 1; i < k; i++) {
                            atual = atual.getProximo();
                        }
                        int pos = (int) atual.getConteudo() - 1;
                        publish(pos);
                        Thread.sleep(delay);
                        No proximo = atual.getProximo();
                        listaRef.remover(atual);
                        atual = proximo;
                    }
                    return null;
                }

                
                protected void process(java.util.List<Integer> chunks) {
                    for (int pos : chunks) {
                        if (pos >= 0) listaPaineisPessoas.get(pos).setBackground(Color.RED);
                    }
                    painelPessoas.repaint();
                }

                
                protected void done() {
                    int survivorIdx = (int) listaRef.getInicio().getConteudo() - 1;
                    JPanel survPanel = listaPaineisPessoas.get(survivorIdx);
                    // Troca label para mostrar imagem do sobrevivente
                    JLabel survLabel = (JLabel) survPanel.getComponent(0);
                    survLabel.setText("");
                    ImageIcon icon = new ImageIcon("sobrevivente.png");
                    Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    survLabel.setIcon(new ImageIcon(img));

                    painelPessoas.revalidate();
                    painelPessoas.repaint();
                    JOptionPane.showMessageDialog(JosephusGUI.this,
                        "Sobrevivente: " + listaRef.getInicio().getConteudo());

                    // Reabilita controles
                    campoN.setEnabled(true);
                    campoK.setEnabled(true);
                    campoDelay.setEnabled(true);
                    botaoResolver.setEnabled(true);
                }
            };
            worker.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite valores válidos para n, k e intervalo.");
        }
    }
    
    /**
     * Método principal da aplicação. Inicializa a interface gráfica Swing.
     * 
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(JosephusGUI::new);
    }
}
