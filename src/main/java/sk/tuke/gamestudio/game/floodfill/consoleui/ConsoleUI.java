package sk.tuke.gamestudio.game.floodfill.consoleui;

import sk.tuke.gamestudio.game.floodfill.core.ColorType;
import sk.tuke.gamestudio.game.floodfill.core.Field;
import sk.tuke.gamestudio.game.floodfill.core.GameState;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleUI {
    private final Field field;
    private final Scanner scanner = new Scanner(System.in);
    private static final Pattern REGEXP = Pattern.compile("([a-z])([a-z])");

    // Colors
    private final String greenString = "\u001B[38;5;48m";
    private final String redString = "\u001B[38;5;9m";


    public ConsoleUI(Field field) {
        this.field = field;
    }

    public void play() {
        boolean again = true;

        while (again) {
            field.generate();

            do {
                render();
                handleInput();
                field.checkState();
            } while(field.getState() == GameState.PLAYING);

            render();

            if (field.getState() == GameState.SOLVED) {
                System.out.println("\n" + greenString + "You won :)" + ColorType.RESET);
            } else if(field.getState() == GameState.FAILED){
                System.out.println("\n" + redString + "You lose :(" + ColorType.RESET);
            }

            while (true) {
                System.out.print("Do you want to play again (Y/N)? ");
                String line =  scanner.nextLine().trim().toLowerCase();

                if (line.equals("y") || line.equals("yes")) {
                    break;
                } else if (line.equals("n") || line.equals("no")) {
                    again = false;
                    break;
                }
            }

            field.setCurrentMoves(0);
            field.setGameState(GameState.PLAYING);
        }
    }

    public void render() {
        System.out.println("\nState: " + greenString + field.getState() + ColorType.RESET + "\n");
        renderHeader();
        renderBody();
    }

    public void renderBody() {
        String borderColor = "\u001B[48;5;247m";
        String borderShadow = "\u001B[48;5;245m";
        System.out.println("  " + borderColor + " ".repeat(field.getColumnCount() * 3 + 4) + ColorType.RESET);
        for (int i = 0; i < field.getRowCount(); i++) {
            System.out.print(((char) ('A' + i)) + " " + borderColor + " " + borderShadow + " " + ColorType.RESET);

            for (int j = 0; j < field.getColumnCount(); j++) {
                ColorType color = field.getGrid()[i][j].getColor();

                System.out.print(color.getAnsiCode() + "   " + ColorType.RESET);
            }

            System.out.println(borderShadow + " " + borderColor + " " + ColorType.RESET);
        }

        System.out.println("  " + borderColor + " ".repeat(field.getColumnCount() * 3 + 4) + ColorType.RESET);
        System.out.println("  " + borderColor + "\u001b[38;5;15m" + "  moves: " + field.getCurrentMoves() + "/" + field.getMaxMoves() + " ".repeat(field.getColumnCount() * 3 - 16 - (field.getCurrentMoves() > 9 ? 1 : 0)) + field.getRowCount() + "x" + field.getColumnCount() + "  " + ColorType.RESET);
    }

    public void renderHeader() {
        System.out.print("    ");
        for (int i = 0; i < field.getColumnCount(); i++) {
            System.out.print(" " + ((char) ('A' + i)) + " ");
        } System.out.println();
    }

    public void handleInput() {
        System.out.print("\nEnter command: ");
        String line =  scanner.nextLine().trim().toLowerCase();

        if ("x".equals(line)) {
            System.exit(0);
        }

        Matcher matcher = REGEXP.matcher(line);
        if (matcher.matches() && line.charAt(0) - 'a' < field.getRowCount() && line.charAt(1) - 'a' < field.getColumnCount()) {
            int row = line.charAt(0) - 'a';
            int col = line.charAt(1) - 'a';

            ColorType newColor = field.getGrid()[row][col].getColor();
            ColorType oldColor = field.getGrid()[0][0].getColor();

            field.floodFill(0, 0, newColor, oldColor);
            if (newColor != oldColor) field.setCurrentMoves(field.getCurrentMoves() + 1);
        } else {
            System.out.println("\n" + redString + "Invalid command!" + ColorType.RESET );
            System.out.println("Use commands: ab, bc, x");
        }
    }
}
