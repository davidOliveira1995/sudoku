import javax.swing.SwingUtilities;

public class SudokuGame {
    public static void main(String[] args) {
        // Exemplo de como passar números iniciais via args:
        // java SudokuGame 5:0:0 3:0:1 8:1:4 2:2:6 ...
        // Formato: "numero:linha:coluna" (linha e coluna de 0 a 8)

        final SudokuBoard board = new SudokuBoard(args);

        SwingUtilities.invokeLater(() -> {
            new SudokuGUI(board).setVisible(true);
        });
    }
}