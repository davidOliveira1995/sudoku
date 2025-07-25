package com.david.sudoku.model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class SudokuBoard {
    private int[][] board;
    private boolean[][] fixedNumbers; // true se o número foi inicializado (fixo)
    private List<Integer>[][] draftNumbers; // Lista de rascunhos para cada célula
    private GameStatus status;
    private boolean hasErrors;

    private static final int BOARD_SIZE = 9;

    public SudokuBoard(String[] initialNumbers) {
        this.board = new int[BOARD_SIZE][BOARD_SIZE];
        this.fixedNumbers = new boolean[BOARD_SIZE][BOARD_SIZE];
        this.draftNumbers = new List[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.draftNumbers[i][j] = new ArrayList<>();
            }
        }
        this.status = GameStatus.NOT_STARTED;
        this.hasErrors = false;
        initializeBoard(initialNumbers);
        checkGameStatus(); // Atualiza o status inicial
    }

    private void initializeBoard(String[] initialNumbers) {
        if (initialNumbers != null) {
            for (String entry : initialNumbers) {
                try {
                    String[] parts = entry.split(":");
                    if (parts.length == 3) {
                        int number = Integer.parseInt(parts[0]);
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);

                        if (isValidCoordinate(row, col) && isValidNumber(number)) {
                            this.board[row][col] = number;
                            this.fixedNumbers[row][col] = true;
                        } else {
                            System.err.println("Aviso: Entrada inicial inválida - " + entry);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erro ao analisar entrada inicial: " + entry + " - " + e.getMessage());
                }
            }
        }
    }

    public boolean placeNumber(int number, int row, int col) {
        if (!isValidCoordinate(row, col) || !isValidNumber(number)) {
            return false; // Coordenadas ou número inválidos
        }
        if (fixedNumbers[row][col]) {
            return false; // Não pode sobrescrever um número fixo
        }
        if (board[row][col] != 0) {
            return false; // Posição já preenchida
        }

        board[row][col] = number;
        checkGameStatus(); // Atualiza o status após a alteração
        return true;
    }

    public boolean removeNumber(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            return false; // Coordenadas inválidas
        }
        if (fixedNumbers[row][col]) {
            return false; // Não pode remover um número fixo
        }
        board[row][col] = 0; // 0 representa uma célula vazia
        checkGameStatus(); // Atualiza o status após a alteração
        return true;
    }

    public void placeDraftNumber(int number, int row, int col) {
        if (isValidCoordinate(row, col) && isValidNumber(number)) {
            if (!draftNumbers[row][col].contains(number)) {
                draftNumbers[row][col].add(number);
                draftNumbers[row][col].sort(null); // Mantém os rascunhos ordenados
            }
        }
    }

    public void removeDraftNumber(int number, int row, int col) {
        if (isValidCoordinate(row, col) && isValidNumber(number)) {
            draftNumbers[row][col].remove(Integer.valueOf(number)); // Remove o objeto Integer
        }
    }

    public int[][] getBoard() {
        // Retorna uma cópia para evitar modificações externas diretas
        int[][] currentBoard = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, currentBoard[i], 0, BOARD_SIZE);
        }
        return currentBoard;
    }

    public List<Integer> getDraftNumbers(int row, int col) {
        if (isValidCoordinate(row, col)) {
            return new ArrayList<>(draftNumbers[row][col]); // Retorna uma cópia
        }
        return new ArrayList<>();
    }

    public boolean isPositionFixed(int row, int col) {
        if (isValidCoordinate(row, col)) {
            return fixedNumbers[row][col];
        }
        return false;
    }

    public void checkGameStatus() {
        boolean allFilled = true;
        boolean currentHasErrors = false;

        // Verifica se todas as células estão preenchidas
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == 0) {
                    allFilled = false;
                    break;
                }
            }
            if (!allFilled) break;
        }

        // Verifica erros em linhas, colunas e blocos 3x3
        if (allFilled || !status.equals(GameStatus.NOT_STARTED)) { // Só verifica erros se não for NOT_STARTED
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (hasDuplicates(getRow(i)) || hasDuplicates(getColumn(i))) {
                    currentHasErrors = true;
                    break;
                }
            }
            if (!currentHasErrors) {
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 3; c++) {
                        if (hasDuplicates(getBlock(r * 3, c * 3))) {
                            currentHasErrors = true;
                            break;
                        }
                    }
                    if (currentHasErrors) break;
                }
            }
        }

        this.hasErrors = currentHasErrors;

        if (status.equals(GameStatus.NOT_STARTED)) {
            // Se o jogo ainda não foi iniciado, o status permanece NOT_STARTED, sem erros.
            this.hasErrors = false;
        } else if (allFilled) {
            this.status = GameStatus.COMPLETE;
        } else {
            this.status = GameStatus.INCOMPLETE;
        }
    }

    public String getStatusMessage() {
        String message = "Status do Jogo: " + status.toString();
        if (status.equals(GameStatus.NOT_STARTED)) {
            message += " (sem erros)";
        } else {
            message += (hasErrors ? " (com erros)" : " (sem erros)");
        }
        return message;
    }

    public boolean gameIsCompleteAndValid() {
        return status.equals(GameStatus.COMPLETE) && !hasErrors;
    }

    public void clearPlayerNumbers() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (!fixedNumbers[r][c]) {
                    board[r][c] = 0;
                }
                draftNumbers[r][c].clear(); // Limpa rascunhos também
            }
        }
        this.status = GameStatus.INCOMPLETE; // Após limpar, o jogo é incompleto
        this.hasErrors = false; // E sem erros, a menos que os fixos já causem um
        checkGameStatus(); // Reavalia o status
    }

    // Métodos auxiliares para verificação de erros
    private int[] getRow(int row) {
        return board[row];
    }

    private int[] getColumn(int col) {
        int[] column = new int[BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            column[r] = board[r][col];
        }
        return column;
    }

    private int[] getBlock(int startRow, int startCol) {
        int[] block = new int[BOARD_SIZE];
        int index = 0;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                block[index++] = board[r][c];
            }
        }
        return block;
    }

    private boolean hasDuplicates(int[] array) {
        Set<Integer> seen = new HashSet<>();
        for (int num : array) {
            if (num != 0) { // Ignora células vazias
                if (seen.contains(num)) {
                    return true;
                }
                seen.add(num);
            }
        }
        return false;
    }

    private boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private boolean isValidNumber(int number) {
        return number >= 1 && number <= 9;
    }

    public GameStatus getStatus() {
        return status;
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}