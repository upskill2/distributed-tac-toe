package com.tic.app.gameengineserviceapp.domain;

import java.util.ArrayList;
import java.util.List;

public class TicTacToeBoard {

    public static final String EMPTY_BOARD = "         ";

    private static final int[][] WIN_CONDITIONS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
        {0, 4, 8}, {2, 4, 6}
    };

    public static String applyMove(String board, int position, Player player) {
        char[] cells = board.toCharArray();
        cells[position] = player.symbol();
        return new String(cells);
    }

    public static GameStatus evaluate(String board) {
        for (int[] condition : WIN_CONDITIONS) {
            char a = board.charAt(condition[0]);
            char b = board.charAt(condition[1]);
            char c = board.charAt(condition[2]);
            if (a != ' ' && a == b && b == c) {
                return a == Player.X.symbol() ? GameStatus.X_WON : GameStatus.O_WON;
            }
        }
        return isBoardFull(board) ? GameStatus.DRAW : GameStatus.ONGOING;
    }

    public static boolean isCellEmpty(String board, int position) {
        return board.charAt(position) == ' ';
    }

    public static boolean isBoardFull(String board) {
        for (char c : board.toCharArray()) {
            if (c == ' ') return false;
        }
        return true;
    }

    public static List<Integer> getAvailablePositions(String board) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == ' ') positions.add(i);
        }
        return positions;
    }

    private TicTacToeBoard() {}
}
