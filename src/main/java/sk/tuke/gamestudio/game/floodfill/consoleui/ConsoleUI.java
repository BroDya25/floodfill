package sk.tuke.gamestudio.game.floodfill.consoleui;

import org.springframework.beans.factory.annotation.Autowired;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.floodfill.core.ColorType;
import sk.tuke.gamestudio.game.floodfill.core.Field;
import sk.tuke.gamestudio.game.floodfill.core.GameState;
import sk.tuke.gamestudio.service.*;

import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleUI {
    // Field
    private Field field;
    private final Scanner scanner = new Scanner(System.in);

    // Regex
    private static final Pattern REGEXP1 = Pattern.compile("([a-z])([a-z])");
    private static final Pattern REGEXP2 = Pattern.compile("([0-9])([0-9])");
    private static final Pattern REGEXP3 = Pattern.compile("([1-5])");

    // Booleans
    private boolean quitGame = false;
    private boolean again = true;
    private boolean renderingMenu = true;
    private boolean startGame = true;

    // Services
    @Autowired
    private ScoreService scoreService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RatingService ratingService;

    // Strings
    private String betweenSpace;
    private String widthEndBorder;
    private String border;
    private final String marginLeft = "  ";
    private String userName;

    // Constants
    private static final int MIN_SIZE = 12;
    private static final int MAX_SIZE = 22;

    // Global Colors
    private final String greenString = "\u001B[38;5;48m";
    private final String redString = "\u001B[38;5;9m";
    private final String borderColor = "\u001B[48;5;247m";
    private final String borderShadow = "\u001B[48;5;245m";
    private final String whiteString = "\u001b[38;5;16m";

    public ConsoleUI(ScoreService scoreService, CommentService commentService, RatingService ratingService) {
        this.scoreService = scoreService;
        this.commentService = commentService;
        this.ratingService = ratingService;
    }

    public ConsoleUI() {

    }

    // Menu
    public void renderMenu() {
        while (renderingMenu) {
            System.out.println("\n" + borderShadow + " " + borderColor + "                     " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "--- MAIN MENU ---  " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + "                     " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "> PLAY             " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "> SCORE            " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "> COMMENTS         " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "> RATING           " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "> RULES            " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "> EXIT             " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + "                     " + borderShadow + " " + ColorType.RESET);
            System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "Name: " + userName + " ".repeat(13 - userName.length()) + borderShadow + " " + ColorType.RESET);

            handleInputMenu();
        }
    }

    private void handleInputMenu() {
        System.out.print("\n" + marginLeft + "Enter your choice: ");
        String choice = scanner.nextLine().trim().toLowerCase();

        switch (choice) {
            case "play" -> play();
            case "score" -> renderScores();
            case "rules" -> renderRules();
            case "comments" -> renderComments();
            case "rating" -> renderRating();
            case "exit" -> {
                System.out.println("\n" + "\u001B[38;5;48m" + marginLeft + "Thanks for playing " + userName + "!" + " Goodbye.");
                renderingMenu = false;
            }
            default ->
                    System.out.println("\n" + "\u001B[38;5;9m" + marginLeft + "Invalid command! Please enter PLAY/SCORE/RULES/EXIT." + ColorType.RESET);
        }
    }

    // Game
    private void play() {
        if (startGame) startPlaying();

        again = true;
        quitGame = false;

        while (again) {
            do {
                render();
                handleInput();
                if (quitGame) return;
                field.checkState();
                field.setGameState(GameState.SOLVED);
            } while (field.getState() == GameState.PLAYING);

            render();

            if (field.getState() == GameState.SOLVED) {
                System.out.println(greenString + marginLeft + "You won :)" + ColorType.RESET);
                saveScore();
            } else if (field.getState() == GameState.FAILED){
                System.out.println(redString + marginLeft + "You lose :(" + ColorType.RESET);
            }

            enbDialogBox();
        }
    }

    private void startPlaying() {
        int row;
        int col;

        while (true) {
            System.out.print("\n" + marginLeft + "Enter size of row: ");
            String rowCount = scanner.nextLine().trim();
            Matcher rowMatcher = REGEXP2.matcher(rowCount);

            System.out.print(marginLeft + "Enter size of col: ");
            String colCount = scanner.nextLine().trim();
            Matcher colMatcher = REGEXP2.matcher(colCount);

            if (rowMatcher.matches() && colMatcher.matches()) {
                row = Integer.parseInt(rowCount);
                col = Integer.parseInt(colCount);

                if (row >= MIN_SIZE && row <= MAX_SIZE && col >= MIN_SIZE && col <= MAX_SIZE) {
                    break;
                }
            }

            System.out.println("\n" + "\u001B[38;5;9m" + "  Invalid input!" + ColorType.RESET);
            System.out.println("  Must have between 12 and 22 sizes");
        }

        field = new Field(row, col);
        field.generate();

        startGame = false;
    }

    private void enbDialogBox() {
        double rating = ratingService.getRating("FloodFill", userName);

        if (rating == 0) {
            while (true) {
                System.out.print("\n" + marginLeft + "Rate the game (1-5) or press ENTER to skip: ");
                String rate = scanner.nextLine().trim().toLowerCase();

                if (REGEXP3.matcher(rate).matches()) {
                    saveRating(Double.parseDouble(rate));
                    break;
                } else if (!rate.isEmpty()) {
                    System.out.println("\n" + "\u001B[38;5;9m" + "  Invalid input!" + ColorType.RESET);
                } else {
                    break;
                }
            }
        }

        System.out.print("\n" + marginLeft + "Leave a comment or press ENTER to skip: ");
        String comment = scanner.nextLine().trim().toLowerCase();

        if (!comment.isEmpty()) saveComment(comment);

        while (true) {
            System.out.print("\n" + marginLeft + "Do you want to play again (Y/N)? ");
            String line =  scanner.nextLine().trim().toLowerCase();

            if (line.equals("y") || line.equals("yes")) {
                field.setCurrentMoves(0);
                field.setGameState(GameState.PLAYING);
                field.generate();
                break;
            } else if (line.equals("n") || line.equals("no")) {
                again = false;
                startGame = true;
                break;
            }
        }
    }

    private void render() {
        renderHeader();
        renderBody();
        System.out.print("\n" + marginLeft + "State: " + (field.getState() == GameState.FAILED ? redString : greenString) + field.getState() + ColorType.RESET + "\n");
    }

    private void renderBody() {
        System.out.println(marginLeft + borderColor + " ".repeat(field.getColumnCount() * 3 + 4) + ColorType.RESET);
        for (int i = 0; i < field.getRowCount(); i++) {
            System.out.print(((char) ('A' + i)) + " " + borderColor + " " + borderShadow + " " + ColorType.RESET);

            for (int j = 0; j < field.getColumnCount(); j++) {
                ColorType color = field.getGrid()[i][j].getColor();

                System.out.print(color.getAnsiCode() + "   " + ColorType.RESET);
            }

            System.out.println(borderShadow + " " + borderColor + " " + ColorType.RESET);
        }

        System.out.println(marginLeft + borderColor + " ".repeat(field.getColumnCount() * 3 + 4) + ColorType.RESET);
        System.out.println(marginLeft + borderColor + whiteString + "  " + field.getCurrentMoves() + "/" + field.getMaxMoves() + " moves" + " ".repeat(field.getColumnCount() * 3 - 15 - (field.getCurrentMoves() > 9 ? 1 : 0)) + field.getRowCount() + "x" + field.getColumnCount() + "  " + ColorType.RESET);
    }

    private void renderHeader() {
        System.out.print("\n    ");
        for (int i = 0; i < field.getColumnCount(); i++) {
            System.out.print(" " + ((char) ('A' + i)) + " ");
        } System.out.println();
    }

    private void handleInput() {
        System.out.print("\n" + marginLeft + "Enter command: ");
        String line =  scanner.nextLine().trim().toLowerCase();

        if ("x".equals(line)) {
            quitGame = true;
            return;
        }

        Matcher matcher = REGEXP1.matcher(line);
        if (matcher.matches() && line.charAt(0) - 'a' < field.getRowCount() && line.charAt(1) - 'a' < field.getColumnCount()) {
            int row = line.charAt(0) - 'a';
            int col = line.charAt(1) - 'a';

            ColorType newColor = field.getGrid()[row][col].getColor();
            ColorType oldColor = field.getGrid()[0][0].getColor();

            field.floodFill(0, 0, newColor, oldColor);

            if (newColor != oldColor) {
                field.setCurrentMoves(field.getCurrentMoves() + 1);
            }
        } else {
            System.out.println("\n" + marginLeft + redString + "Invalid command!" + ColorType.RESET);
            System.out.println(marginLeft + "Use commands: ab, bc, x");
        }
    }

    public void saveScore() {
        scoreService.addScore(new Score("FloodFill", userName, field.getScore(), new Date()));
    }

    public void saveComment(String comment) {
        commentService.addComment(new Comment("FloodFill", userName, comment, new Date()));
    }

    public void saveRating(double rating) {
        ratingService.setRating(new Rating("FloodFill", userName, rating, new Date()));
    }

    // Score
    private void renderScores() {
        List<Score> scores = scoreService.getTopScores("FloodFill");

        if (scores.isEmpty()) return;

        String columnPlayerTitle = "Player";
        String columnTitleScore = "Score";
        int maxLengthPlayerName = scores.stream().map(Score::getPlayer).mapToInt(String::length).max().orElse(0);
        int maxLengthPoints = scores.stream().map(Score::getPoints).map(String::valueOf).mapToInt(String::length).max().orElse(1);

        renderHeaderTable(columnPlayerTitle, columnTitleScore, maxLengthPlayerName, maxLengthPoints);

        for (int i = 0; i < scores.size(); i++) {
            Score score = scores.get(i);
            System.out.printf(borderShadow + " " + borderColor + whiteString + " %d. " + (i+1 > 9 ? "" : " ") + "%s" + " ".repeat(columnPlayerTitle.length() + betweenSpace.length() - score.getPlayer().length()) + "%d" + " ".repeat(columnTitleScore.length() + widthEndBorder.length() - ("" + score.getPoints()).length()) + borderShadow + " " + ColorType.RESET + "\n", i + 1, score.getPlayer(), score.getPoints());
        }

        System.out.println(border);
    }

    // Comment
    private void renderComments() {
        List<Comment> comments = commentService.getComments("FloodFill");

        if (comments.isEmpty()) return;

        String columnPlayerTitle = "Player";
        String columnTitleScore = "Comment";
        int maxLengthPlayerName = comments.stream().map(Comment::getPlayer).mapToInt(String::length).max().orElse(0);
        int maxLengthComment = comments.stream().map(Comment::getComment).mapToInt(String::length).max().orElse(0);

        renderHeaderTable(columnPlayerTitle, columnTitleScore, maxLengthPlayerName, maxLengthComment);

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            System.out.printf(borderShadow + " " + borderColor + whiteString + " %d. " + (i+1 > 9 ? "" : " ") + "%s" + " ".repeat(columnPlayerTitle.length() + betweenSpace.length() - comment.getPlayer().length()) + "%s" + " ".repeat(columnTitleScore.length() + widthEndBorder.length() - comment.getComment().length()) + borderShadow + " " + ColorType.RESET + "\n", i + 1, comment.getPlayer(), comment.getComment());
        }

        System.out.println(border);
    }

    // Rating
    private void renderRating() {
        double averageRating = Math.round(ratingService.getAverageRating("FloodFill") * 10.0) / 10.0;
        double yourRating = Math.round(ratingService.getRating("FloodFill", userName) * 10.0) / 10.0;

        System.out.println("\n" + borderShadow + " " + borderColor + marginLeft + "                     " + borderShadow + " " + ColorType.RESET);
        System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "Average rating: " + averageRating + "  " + borderShadow + " " + ColorType.RESET);
        System.out.println(borderShadow + " " + borderColor + marginLeft + "                     " + borderShadow + " " + ColorType.RESET);
        System.out.println(borderShadow + " " + borderColor + whiteString + marginLeft + "Your rating: " + yourRating + "     " + borderShadow + " " + ColorType.RESET);
        System.out.println(borderShadow + " " + borderColor + marginLeft + "                     " + borderShadow + " " + ColorType.RESET);
    }

    // Rules
    private void renderRules() {
        System.out.println("\n" +
                borderColor + "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "1. Inicializácia hry                                                                       " + ColorType.RESET + "\n" +
                borderColor + "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Keď sa hra spustí:                                                                         " + ColorType.RESET + "\n" +
                borderColor + "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Vytvorí sa herná doska s rozmermi N x N.                                                 " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Každej bunke sa náhodne priradí jedna z platných farieb.                                 " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Nastaví sa maximálny počet ťahov (maxMoves).                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Počítadlo ťahov (currentMoves) sa vynuluje.                                              " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Stav hry sa nastaví na PLAYING.                                                          " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "2. Základná hrateľnosť                                                                     " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Hráč začína hru z ľavého horného políčka ihriska (súradnice **(0,0)**).                    " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Jedno kolo hry sa vykoná nasledovne:                                                       " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "1. Hráč si vyberie novú farbu.                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "2. Ak sa vybraná farba zhoduje s aktuálnou farbou východiskovej oblasti, ťah sa nevykoná.  " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "3. V opačnom prípade:                                                                      " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- spustí sa algoritmus **Flood Fill**;                                                     " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- zmení sa farba celej pripojenej oblasti začínajúcej na **(0,0)**;                        " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- počítadlo ťahov sa zvýši o 1.                                                            " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "3. Algoritmus zaplavenia                                                                   " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Algoritmus vykonáva nasledujúce kroky:                                                     " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "1. Určuje počiatočnú farbu počiatočnej bunky.                                              " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "2. Rekurzívne (alebo cez zásobník/front):                                                  " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- kontroluje susedné bunky (hore, dole, vľavo, vpravo);                                    " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- ak má susedná bunka počiatočnú farbu,:                                                   " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- je prefarbená novou farbou;                                                              " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- je pridaná do spracovania.                                                               " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "3. Proces pokračuje, kým nie sú spracované všetky prepojené bunky počiatočnej farby.       " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Týmto sa rozšíri „zachytená“ oblasť.                                                       " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "4. Kontrola podmienok ukončenia                                                            " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Po každom ťahu sa kontroluje stav hry.                                                     " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Víťazstvo                                                                                  " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Ak majú všetky políčka na hracej doske rovnakú farbu,                                      " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "stav hry sa zmení na `SOLVED`.                                                             " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Porážka                                                                                    " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Ak počet ťahov dosiahol `maxMoves`,                                                        " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "a hracia doska nie je úplne vyfarbená,                                                     " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "stav hry sa zmení na `FAILED`.                                                             " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "5. Koniec hry                                                                              " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Hra končí v dvoch prípadoch:                                                               " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Hráč vyhráva (celá hracia doska je jednej farby).                                        " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Hráč prehráva (už nie sú možné žiadne ďalšie ťahy).                                      " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Po skončení hry sú možné nasledujúce možnosti:                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Reštartovať hru;                                                                         " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Ukončiť program.                                                                         " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "Základné pravidlá hry                                                                      " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Východiskový bod je vždy pevný — **(0,0)**.                                              " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Povolené sú iba susediace bunky po stranách (žiadne uhlopriečky).                        " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Každý ťah zmení farbu celej aktuálne prepojenej oblasti.                                 " + ColorType.RESET + "\n" +
                borderColor + whiteString + marginLeft + "- Cieľom hry je urobiť hraciu dosku monochromatickou v minimálnom počte ťahov.             " + ColorType.RESET + "\n" +
                borderColor +  "                                                                                             " + ColorType.RESET);
    }

    // Others
    private void renderHeaderTable(String title1, String title2, int max1, int max2) {
        betweenSpace = " ".repeat(max1 > title1.length() ? max1 - title1.length() + 5 : 5);
        widthEndBorder = " ".repeat(max2 > title2.length() ? max2 - title2.length() + 5 : 5);
        border = borderShadow + " " + ColorType.RESET + borderColor + " ".repeat(5 + title1.length() + betweenSpace.length() + title2.length()) + widthEndBorder + borderShadow + " " + ColorType.RESET;

        System.out.println("\n" + border);
        System.out.println(borderShadow + " " + ColorType.RESET + borderColor + whiteString + "     " + title1 + betweenSpace + title2 + widthEndBorder + borderShadow + " " + ColorType.RESET);
        System.out.println(border);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
