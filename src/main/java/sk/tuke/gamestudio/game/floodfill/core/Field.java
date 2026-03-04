package sk.tuke.gamestudio.game.floodfill.core;

public class Field {
    private int rowCount;
    private int columnCount;
    private int countColors;
    private Cell[][] grid;

    public Field(int rowCount, int columnCount, int countColors) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.countColors = countColors;
    }

    public void generate() {}

    public void floodFill(int row, int column, ColorType newColor) {}

    public boolean isSolved() { return false; }

    public Cell getCell(int row, int column) { return null; }
}
