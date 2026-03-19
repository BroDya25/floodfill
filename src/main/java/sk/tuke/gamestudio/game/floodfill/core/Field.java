package sk.tuke.gamestudio.game.floodfill.core;

import static java.lang.Math.round;

import java.util.Random;

public class Field {
    private final int rowCount;
    private final  int columnCount;
    private Cell[][] grid;
    private GameState state;
    private int currentMoves;
    private int maxMoves;
    private final Random random;

    private static final int MIN_SIZE = 12;
    private static final int MAX_SIZE = 22;

    public Field(int rowCount, int columnCount) {
        if (rowCount < MIN_SIZE || columnCount < MIN_SIZE) throw new IllegalArgumentException("Row or column count is smaller than 12!");
        if (rowCount > MAX_SIZE || columnCount > MAX_SIZE) throw new IllegalArgumentException("Row or column count is higher than 22!");

        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.state = GameState.PLAYING;
        random = new Random();
    }

    public void generate() {
        grid = new Cell[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                grid[i][j] = new Cell(i, j, ColorType.values()[random.nextInt(ColorType.values().length)]);
            }
        }

        maxMoves = (int)round((double)(rowCount + columnCount) / 2 * 1.8);
    }

    public void floodFill(int row, int col, ColorType newColor, ColorType oldColor) {
        if (row < 0 || col < 0 || row >= rowCount || col >= columnCount || newColor == null || oldColor == null) return;

        if (oldColor == grid[row][col].getColor() && newColor != oldColor && grid[row][col] != null && grid[row][col].getColor() != null) {
            grid[row][col].setColor(newColor);

            floodFill(row + 1, col, newColor, oldColor);
            floodFill(row - 1, col, newColor, oldColor);
            floodFill(row, col + 1, newColor, oldColor);
            floodFill(row, col - 1, newColor, oldColor);
        }
    }

    public void checkState() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                 if (grid[0][0].getColor() != grid[i][j].getColor()) {
                     if (currentMoves >= maxMoves) {
                         state = GameState.FAILED;
                         return;
                     } else {
                         state = GameState.PLAYING;
                         return;
                     }
                 }
            }
        }

        state = GameState.SOLVED;
    }

    public Cell getCell(int row, int column) {
        return grid[row][column];
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public GameState getState() {
        return state;
    }

    public int getCurrentMoves() {
        return currentMoves;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public void setCurrentMoves(int currentMoves) {
        this.currentMoves = currentMoves;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }
}
