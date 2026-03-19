package sk.tuke.gamestudio.game.floodfill.core;

public enum ColorType {
    RED("\u001B[48;5;9m"),
    BLUE("\u001B[48;5;27m"),
    GREEN("\u001B[48;5;40m"),
    YELLOW("\u001B[48;5;226m"),
    PURPLE("\u001B[48;5;93m"),
    ORANGE("\u001B[48;5;214m"),
    PINK("\u001B[48;5;201m");

    private final String ansiCode;
    public static final String RESET = "\u001B[0m";

    ColorType(String ansiCode) {
        this.ansiCode = ansiCode;
    }

    public String getAnsiCode() {
        return ansiCode;
    }
}
