import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SudokuGUI extends JFrame {
    private SudokuBoard sudokuBoard;
    private JTextField[][] cells; // Para números principais
    private JPanel[][] draftPanels; // Para rascunhos
    private JLabel[][] draftLabels; // Labels individuais para cada rascunho em cada célula

    private static final int BOARD_SIZE = 9;
    private static final int CELL_SIZE = 60; // Tamanho de cada célula
    private static final Font NUMBER_FONT = new Font("Arial", Font.BOLD, 30);
    private static final Font DRAFT_FONT = new Font("Arial", Font.PLAIN, 10);

    public SudokuGUI(SudokuBoard board) {
        this.sudokuBoard = board;
        this.cells = new JTextField[BOARD_SIZE][BOARD_SIZE];
        this.draftPanels = new JPanel[BOARD_SIZE][BOARD_SIZE];
        this.draftLabels = new JLabel[BOARD_SIZE][BOARD_SIZE * BOARD_SIZE]; // 9 labels por célula para rascunhos

        setTitle("Jogo de Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createMenuBar();
        createGamePanel();
        drawBoard(); // Desenha o tabuleiro inicial

        pack();
        setLocationRelativeTo(null); // Centraliza a janela
        setResizable(false);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Jogo");

        JMenuItem newGameItem = new JMenuItem("Iniciar Novo Jogo");
        newGameItem.addActionListener(e -> startNewGame());

        JMenuItem placeNumberItem = new JMenuItem("Colocar Número");
        placeNumberItem.addActionListener(e -> promptForNumberPlacement());

        JMenuItem removeNumberItem = new JMenuItem("Remover Número");
        removeNumberItem.addActionListener(e -> promptForRemoval());

        JMenuItem placeDraftItem = new JMenuItem("Colocar Rascunho");
        placeDraftItem.addActionListener(e -> promptForDraftPlacement());

        JMenuItem removeDraftItem = new JMenuItem("Remover Rascunho");
        removeDraftItem.addActionListener(e -> promptForDraftRemoval());

        JMenuItem checkGameItem = new JMenuItem("Visualizar Jogo");
        checkGameItem.addActionListener(e -> displayCurrentBoard());

        JMenuItem checkStatusItem = new JMenuItem("Verificar Status do Jogo");
        checkStatusItem.addActionListener(e -> displayGameStatus());

        JMenuItem clearItem = new JMenuItem("Limpar Números do Jogador");
        clearItem.addActionListener(e -> clearPlayerNumbers());

        JMenuItem finishGameItem = new JMenuItem("Finalizar Jogo");
        finishGameItem.addActionListener(e -> finishGame());

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(placeNumberItem);
        gameMenu.add(removeNumberItem);
        gameMenu.addSeparator();
        gameMenu.add(placeDraftItem);
        gameMenu.add(removeDraftItem);
        gameMenu.addSeparator();
        gameMenu.add(checkGameItem);
        gameMenu.add(checkStatusItem);
        gameMenu.add(clearItem);
        gameMenu.addSeparator();
        gameMenu.add(finishGameItem);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void createGamePanel() {
        JPanel gamePanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        gamePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3)); // Borda externa do tabuleiro

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                JPanel cellPanel = new JPanel(new BorderLayout()); // Painel para cada célula
                cellPanel.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));

                // Configura as bordas para os blocos 3x3
                Border border = new LineBorder(Color.LIGHT_GRAY, 1);
                if (r % 3 == 0) border = BorderFactory.createMatteBorder(3, 1, 1, 1, Color.BLACK);
                if (c % 3 == 0) border = BorderFactory.createMatteBorder(1, 3, 1, 1, Color.BLACK);
                if (r % 3 == 0 && c % 3 == 0) border = BorderFactory.createMatteBorder(3, 3, 1, 1, Color.BLACK);
                if (r == 0 && c % 3 != 0) border = BorderFactory.createMatteBorder(3, 1, 1, 1, Color.BLACK);
                if (c == 0 && r % 3 != 0) border = BorderFactory.createMatteBorder(1, 3, 1, 1, Color.BLACK);

                // Bordas para o final do tabuleiro
                if (r == BOARD_SIZE - 1) border = BorderFactory.createMatteBorder(
                        (r % 3 == 0) ? 3 : 1,
                        (c % 3 == 0) ? 3 : 1,
                        3,
                        (c == BOARD_SIZE - 1) ? 3 : 1,
                        Color.BLACK
                );
                if (c == BOARD_SIZE - 1) border = BorderFactory.createMatteBorder(
                        (r % 3 == 0) ? 3 : 1,
                        (c % 3 == 0) ? 3 : 1,
                        (r == BOARD_SIZE - 1) ? 3 : 1,
                        3,
                        Color.BLACK
                );


                // Adiciona o JTextField para o número principal
                JTextField cell = new JTextField();
                cell.setFont(NUMBER_FONT);
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setEditable(false); // Não editável diretamente, apenas via menu
                cell.setBackground(Color.WHITE); // Cor de fundo padrão
                cell.setBorder(border); // Aplica as bordas
                this.cells[r][c] = cell;
                cellPanel.add(cell, BorderLayout.CENTER);

                // Adiciona o painel para os rascunhos (GridLayout 3x3)
                JPanel draftsGrid = new JPanel(new GridLayout(3, 3));
                draftsGrid.setBackground(Color.WHITE);
                this.draftPanels[r][c] = draftsGrid;
                for (int i = 0; i < 9; i++) {
                    JLabel draftLabel = new JLabel("", SwingConstants.CENTER);
                    draftLabel.setFont(DRAFT_FONT);
                    draftLabel.setForeground(Color.GRAY);
                    this.draftLabels[r][c * 9 + i] = draftLabel; // Armazena labels para acesso individual
                    draftsGrid.add(draftLabel);
                }
                cellPanel.add(draftsGrid, BorderLayout.NORTH); // Rascunhos no topo da célula

                gamePanel.add(cellPanel);
            }
        }
        add(gamePanel, BorderLayout.CENTER);
    }

    public void drawBoard() {
        int[][] currentBoard = sudokuBoard.getBoard();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                JTextField cell = cells[r][c];
                int number = currentBoard[r][c];

                if (number != 0) {
                    cell.setText(String.valueOf(number));
                    cell.setForeground(sudokuBoard.isPositionFixed(r, c) ? Color.BLUE.darker() : Color.BLACK); // Azul para fixos, preto para jogador
                    cell.setBackground(Color.WHITE);
                    draftPanels[r][c].setVisible(false); // Esconde rascunhos se houver número principal
                } else {
                    cell.setText("");
                    cell.setBackground(Color.WHITE);
                    draftPanels[r][c].setVisible(true); // Mostra rascunhos se a célula estiver vazia
                }

                // Atualiza os rascunhos
                updateDraftNumbersDisplay(r, c, sudokuBoard.getDraftNumbers(r, c));
            }
        }
        highlightErrors(); // Destaca erros após desenhar
    }

    private void updateDraftNumbersDisplay(int row, int col, List<Integer> drafts) {
        for (int i = 0; i < 9; i++) {
            JLabel draftLabel = draftLabels[row][col * 9 + i];
            if (drafts.contains(i + 1)) {
                draftLabel.setText(String.valueOf(i + 1));
            } else {
                draftLabel.setText("");
            }
        }
    }

    private void highlightErrors() {
        // Primeiro, remove qualquer destaque de erro anterior
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (cells[r][c].getBackground().equals(Color.RED.brighter())) {
                    cells[r][c].setBackground(Color.WHITE);
                }
            }
        }

        if (sudokuBoard.hasErrors()) {
            int[][] currentBoard = sudokuBoard.getBoard();
            // Verifica linhas e colunas
            for (int i = 0; i < BOARD_SIZE; i++) {
                // Linhas
                Set<Integer> seenInRow = new HashSet<>();
                for (int c = 0; c < BOARD_SIZE; c++) {
                    int num = currentBoard[i][c];
                    if (num != 0) {
                        if (seenInRow.contains(num)) {
                            // Marca todos os números duplicados na linha
                            for (int k = 0; k <= c; k++) {
                                if (currentBoard[i][k] == num) {
                                    cells[i][k].setBackground(Color.RED.brighter());
                                }
                            }
                        }
                        seenInRow.add(num);
                    }
                }

                // Colunas
                Set<Integer> seenInCol = new HashSet<>();
                for (int r = 0; r < BOARD_SIZE; r++) {
                    int num = currentBoard[r][i];
                    if (num != 0) {
                        if (seenInCol.contains(num)) {
                            // Marca todos os números duplicados na coluna
                            for (int k = 0; k <= r; k++) {
                                if (currentBoard[k][i] == num) {
                                    cells[k][i].setBackground(Color.RED.brighter());
                                }
                            }
                        }
                        seenInCol.add(num);
                    }
                }
            }

            // Verifica blocos 3x3
            for (int blockRow = 0; blockRow < 3; blockRow++) {
                for (int blockCol = 0; blockCol < 3; blockCol++) {
                    Set<Integer> seenInBlock = new HashSet<>();
                    for (int r = blockRow * 3; r < blockRow * 3 + 3; r++) {
                        for (int c = blockCol * 3; c < blockCol * 3 + 3; c++) {
                            int num = currentBoard[r][c];
                            if (num != 0) {
                                if (seenInBlock.contains(num)) {
                                    // Marca todos os números duplicados no bloco
                                    for (int r2 = blockRow * 3; r2 <= r; r2++) {
                                        for (int c2 = blockCol * 3; c2 <= c; c2++) {
                                            if (currentBoard[r2][c2] == num) {
                                                cells[r2][c2].setBackground(Color.RED.brighter());
                                            }
                                        }
                                    }
                                }
                                seenInBlock.add(num);
                            }
                        }
                    }
                }
            }
        }
    }


    private void startNewGame() {
        // Para iniciar um "novo jogo", o usuário deve reiniciar a aplicação com novos argumentos
        // Ou, se o "novo jogo" significa resetar o atual, podemos chamar clearPlayerNumbers()
        // Por simplicidade, vou fazer com que "Iniciar Novo Jogo" limpe os números do jogador
        // e mantenha os fixos, como um "reset" do jogo atual.
        // Se a intenção é carregar um *novo* tabuleiro, a lógica de inicialização precisaria ser refeita,
        // possivelmente com uma nova instância de SudokuBoard ou um método reset mais robusto.
        sudokuBoard.clearPlayerNumbers();
        sudokuBoard.checkGameStatus(); // Reavalia o status após a limpeza
        drawBoard();
        displayMessage("Jogo reiniciado. Números do jogador foram limpos.", "Novo Jogo");
    }

    private void promptForNumberPlacement() {
        String inputNum = JOptionPane.showInputDialog(this, "Digite o número (1-9):");
        if (inputNum == null || inputNum.isEmpty()) return;
        String inputRow = JOptionPane.showInputDialog(this, "Digite a linha (0-8):");
        if (inputRow == null || inputRow.isEmpty()) return;
        String inputCol = JOptionPane.showInputDialog(this, "Digite a coluna (0-8):");
        if (inputCol == null || inputCol.isEmpty()) return;

        try {
            int number = Integer.parseInt(inputNum);
            int row = Integer.parseInt(inputRow);
            int col = Integer.parseInt(inputCol);

            if (sudokuBoard.placeNumber(number, row, col)) {
                drawBoard();
                displayMessage("Número " + number + " colocado em (" + row + ", " + col + ").", "Sucesso");
            } else {
                displayMessage("Não foi possível colocar o número. Posição inválida, já preenchida ou número fixo.", "Erro ao Colocar Número");
            }
        } catch (NumberFormatException ex) {
            displayMessage("Entrada inválida. Por favor, digite números.", "Erro de Entrada");
        }
    }

    private void promptForRemoval() {
        String inputRow = JOptionPane.showInputDialog(this, "Digite a linha (0-8) do número a remover:");
        if (inputRow == null || inputRow.isEmpty()) return;
        String inputCol = JOptionPane.showInputDialog(this, "Digite a coluna (0-8) do número a remover:");
        if (inputCol == null || inputCol.isEmpty()) return;

        try {
            int row = Integer.parseInt(inputRow);
            int col = Integer.parseInt(inputCol);

            if (sudokuBoard.isPositionFixed(row, col)) {
                displayMessage("Este é um número fixo e não pode ser removido.", "Erro ao Remover");
            } else if (sudokuBoard.removeNumber(row, col)) {
                drawBoard();
                displayMessage("Número removido de (" + row + ", " + col + ").", "Sucesso");
            } else {
                displayMessage("Não foi possível remover o número. Posição inválida ou vazia.", "Erro ao Remover");
            }
        } catch (NumberFormatException ex) {
            displayMessage("Entrada inválida. Por favor, digite números.", "Erro de Entrada");
        }
    }

    private void promptForDraftPlacement() {
        String inputNum = JOptionPane.showInputDialog(this, "Digite o número de rascunho (1-9):");
        if (inputNum == null || inputNum.isEmpty()) return;
        String inputRow = JOptionPane.showInputDialog(this, "Digite a linha (0-8):");
        if (inputRow == null || inputRow.isEmpty()) return;
        String inputCol = JOptionPane.showInputDialog(this, "Digite a coluna (0-8):");
        if (inputCol == null || inputCol.isEmpty()) return;

        try {
            int number = Integer.parseInt(inputNum);
            int row = Integer.parseInt(inputRow);
            int col = Integer.parseInt(inputCol);

            if (sudokuBoard.getBoard()[row][col] != 0) {
                displayMessage("Não é possível adicionar rascunhos em uma célula já preenchida com um número principal.", "Erro de Rascunho");
                return;
            }

            sudokuBoard.placeDraftNumber(number, row, col);
            drawBoard();
            displayMessage("Rascunho " + number + " adicionado em (" + row + ", " + col + ").", "Sucesso");
        } catch (NumberFormatException ex) {
            displayMessage("Entrada inválida. Por favor, digite números.", "Erro de Entrada");
        }
    }

    private void promptForDraftRemoval() {
        String inputNum = JOptionPane.showInputDialog(this, "Digite o número de rascunho a remover (1-9):");
        if (inputNum == null || inputNum.isEmpty()) return;
        String inputRow = JOptionPane.showInputDialog(this, "Digite a linha (0-8):");
        if (inputRow == null || inputRow.isEmpty()) return;
        String inputCol = JOptionPane.showInputDialog(this, "Digite a coluna (0-8):");
        if (inputCol == null || inputCol.isEmpty()) return;

        try {
            int number = Integer.parseInt(inputNum);
            int row = Integer.parseInt(inputRow);
            int col = Integer.parseInt(inputCol);

            sudokuBoard.removeDraftNumber(number, row, col);
            drawBoard();
            displayMessage("Rascunho " + number + " removido de (" + row + ", " + col + ").", "Sucesso");
        } catch (NumberFormatException ex) {
            displayMessage("Entrada inválida. Por favor, digite números.", "Erro de Entrada");
        }
    }


    private void displayCurrentBoard() {
        int[][] currentBoard = sudokuBoard.getBoard();
        StringBuilder sb = new StringBuilder("Situação Atual do Jogo:\n\n");
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                sb.append(currentBoard[r][c] == 0 ? "." : currentBoard[r][c]).append(" ");
                if ((c + 1) % 3 == 0 && c != BOARD_SIZE - 1) {
                    sb.append("| ");
                }
            }
            sb.append("\n");
            if ((r + 1) % 3 == 0 && r != BOARD_SIZE - 1) {
                sb.append("---------------------\n");
            }
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Visualizar Jogo", JOptionPane.PLAIN_MESSAGE);
    }

    private void displayGameStatus() {
        sudokuBoard.checkGameStatus(); // Garante que o status está atualizado
        displayMessage(sudokuBoard.getStatusMessage(), "Status do Jogo");
    }

    private void clearPlayerNumbers() {
        sudokuBoard.clearPlayerNumbers();
        drawBoard();
        displayMessage("Todos os números informados pelo jogador foram removidos.", "Limpeza Concluída");
    }

    private void finishGame() {
        sudokuBoard.checkGameStatus(); // Garante que o status está atualizado
        if (sudokuBoard.gameIsCompleteAndValid()) {
            displayMessage("Parabéns! O jogo está completo e correto. Encerrando o jogo.", "Jogo Finalizado");
            System.exit(0); // Encerra a aplicação
        } else {
            displayMessage("O jogo não está completo e/ou contém erros. Por favor, preencha todos os espaços corretamente.", "Erro ao Finalizar Jogo");
        }
    }

    private void displayMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}