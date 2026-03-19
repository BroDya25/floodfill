package sk.tuke.gamestudio.game.floodfill;

import sk.tuke.gamestudio.game.floodfill.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.floodfill.core.ColorType;
import sk.tuke.gamestudio.game.floodfill.core.Field;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Game {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Pattern REGEXP = Pattern.compile("([0-9])([0-9])");

    private static final int MIN_SIZE = 12;
    private static final int MAX_SIZE = 22;

    public static void main(String[] args) {
        int row;
        int col;

        System.out.println("\u001B[38;5;48m" + "Welcome to FloodFill Game!" + ColorType.RESET);

        while (true) {
            System.out.print("Enter size of row: ");
            String rowCount = scanner.nextLine().trim();
            Matcher rowMatcher = REGEXP.matcher(rowCount);

            System.out.print("Enter size of col: ");
            String colCount = scanner.nextLine().trim();
            Matcher colMatcher = REGEXP.matcher(colCount);

            if (rowMatcher.matches() && colMatcher.matches()) {
                row = Integer.parseInt(rowCount);
                col = Integer.parseInt(colCount);

                if (row >= MIN_SIZE && row <= MAX_SIZE && col >= MIN_SIZE && col <= MAX_SIZE) {
                    break;
                }
            }

            System.out.println("\n" + "\u001B[38;5;9m" + "Invalid input!" + ColorType.RESET);
            System.out.println("Must have between 12 and 22 sizes\n");
        }

        Field field = new Field(row, col);
        ConsoleUI console = new ConsoleUI(field);

        console.play();
    }
}
