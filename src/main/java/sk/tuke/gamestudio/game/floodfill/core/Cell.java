package sk.tuke.gamestudio.game.floodfill.core;

public class Cell {
    private int row;
    private int column;
    private ColorType color;

    public Cell(int row, int column, ColorType color) {
        this.row = row;
        this.column = column;
        this.color = color;
    }

    public void setColor(ColorType color) {
        this.color = color;
    }

    public ColorType getColor() { return color; }
}
