package sk.tuke.gamestudio.game.floodfill.core;

import static java.lang.Math.round;

import java.util.*;

public class Field {
    private final int rowCount;
    private final  int columnCount;
    private final Cell[][] grid;
    private GameState state;
    private int currentMoves;
    private final int maxMoves;
    private final Random random;
    private long startTime;
    private ColorType[][] initialColors;

    private final Deque<ColorType[][]> undoStack = new ArrayDeque<>();
    private final Deque<ColorType[][]> redoStack = new ArrayDeque<>();
    private final Deque<Integer>       undoMoves = new ArrayDeque<>();
    private final Deque<Integer>       redoMoves = new ArrayDeque<>();

    private static final int MIN_SIZE = 12;
    private static final int MAX_SIZE = 22;
    private static final int MAX_HISTORY = 50;

    public Field(int rowCount, int columnCount) {
        if (rowCount < MIN_SIZE || columnCount < MIN_SIZE) throw new IllegalArgumentException("Row or column count is smaller than 12!");
        if (rowCount > MAX_SIZE || columnCount > MAX_SIZE) throw new IllegalArgumentException("Row or column count is higher than 22!");

        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.grid = new Cell[rowCount][columnCount];
        this.state = GameState.PLAYING;
        this.maxMoves = (int)round((double)(rowCount + columnCount) / 2 * 2.2);
        random = new Random();
        startTime = System.currentTimeMillis();
        generate();
    }

    public void generate() {
        initialColors = new ColorType[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                ColorType color = ColorType.values()[random.nextInt(ColorType.values().length)];
                grid[i][j] = new Cell(i, j, color);
                initialColors[i][j] = color;
            }
        }
        undoStack.clear(); redoStack.clear();
        undoMoves.clear(); redoMoves.clear();
    }

    public void reset() {
        if (initialColors == null) return;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                grid[i][j].setColor(initialColors[i][j]);
            }
        }
        this.currentMoves = 0;
        this.state = GameState.PLAYING;
        this.startTime = System.currentTimeMillis();
        undoStack.clear(); redoStack.clear();
        undoMoves.clear(); redoMoves.clear();
    }

    public void saveSnapshot() {
        if (undoStack.size() >= MAX_HISTORY) {
            undoStack.pollLast();
            undoMoves.pollLast();
        }
        undoStack.push(captureColors());
        undoMoves.push(currentMoves);
        redoStack.clear();
        redoMoves.clear();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void undo() {
        if (!canUndo()) return;
        redoStack.push(captureColors());
        redoMoves.push(currentMoves);
        applyColors(undoStack.pop());
        currentMoves = undoMoves.pop();
        state = GameState.PLAYING;
    }

    public void redo() {
        if (!canRedo()) return;
        undoStack.push(captureColors());
        undoMoves.push(currentMoves);
        applyColors(redoStack.pop());
        currentMoves = redoMoves.pop();
        checkState();
    }

    private ColorType[][] captureColors() {
        ColorType[][] snap = new ColorType[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
                snap[i][j] = grid[i][j].getColor();
        return snap;
    }

    private void applyColors(ColorType[][] snap) {
        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < columnCount; j++)
                grid[i][j].setColor(snap[i][j]);
    }

    public void floodFill(int row, int col, ColorType newColor, ColorType oldColor) {
        if (row < 0 || col < 0 || row >= rowCount || col >= columnCount || newColor == null || oldColor == null || grid[row][col] == null || grid[row][col].getColor() == null) return;

        if (oldColor == grid[row][col].getColor() && newColor != oldColor) {
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

    public int getScore() {
        int points = rowCount * columnCount - (int) (System.currentTimeMillis() - startTime) / 1000;
        return Math.max(points, 0);
    }

    public void setCurrentMoves(int currentMoves) {
        this.currentMoves = currentMoves;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }
}
