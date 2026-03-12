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

    public Field(int rowCount, int columnCount) {
        if (rowCount < 12 || columnCount < 12) throw new IllegalArgumentException("Row or column count is smaller than 12!");
        if (rowCount > 22 || columnCount > 22) throw new IllegalArgumentException("Row or column count is higher than 22!");
        if (rowCount != columnCount) throw new IllegalArgumentException("Row and column must be equal!");

        this.rowCount = rowCount;
        this.columnCount = columnCount;
    }

    public void generate() {
        grid = new Cell[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                Random random = new Random();
                grid[i][j] = new Cell(i, j, ColorType.values()[random.nextInt(ColorType.values().length)]);
            }
        }

        maxMoves = (int)round((double)(rowCount + columnCount) / 2 * 1.8);
    }

    public void floodFill(int row, int column, ColorType newColor) {
        if (row < 0 || column < 0 || row >= rowCount || column >= columnCount || newColor == null) return;

        if (grid[row][column].getColor() != newColor && grid[row][column] != null && grid[row][column].getColor() != null) {
            grid[row][column].setColor(newColor);

            floodFill(row + 1, column, newColor);
            floodFill(row - 1, column, newColor);
            floodFill(row, column + 1, newColor);
            floodFill(row, column - 1, newColor);
        }
    }

    public GameState checkState() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                 if (grid[0][0].getColor() != grid[i][j].getColor()) {
                     return currentMoves == maxMoves ? GameState.FAILED : GameState.PLAYING;
                 }
            }
        }

        return GameState.SOLVED;
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

    public int getCurrentMoves() {
        return currentMoves;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public void setCurrentMoves(int currentMoves) {
        this.currentMoves = currentMoves;
    }
}
