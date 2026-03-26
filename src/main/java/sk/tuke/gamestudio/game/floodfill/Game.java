package sk.tuke.gamestudio.game.floodfill;

import sk.tuke.gamestudio.game.floodfill.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.floodfill.core.ColorType;

import java.awt.*;
import java.util.Scanner;

public class Game {

    private static String userName;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String art = "\u001B[38;5;48m" +
                        ":::::::::: :::         ::::::::   ::::::::  :::::::::  :::::::::: ::::::::::: :::        :::\n" +
                        ":+:        :+:        :+:    :+: :+:    :+: :+:    :+: :+:            :+:     :+:        :+:\n" +
                        "+:+        +:+        +:+    +:+ +:+    +:+ +:+    +:+ +:+            +:+     +:+        +:+\n" +
                        ":#::+::#   +#+        +#+    +:+ +#+    +:+ +#+    +:+ :#::+::#       +#+     +#+        +#+\n" +
                        "+#+        +#+        +#+    +#+ +#+    +#+ +#+    +#+ +#+            +#+     +#+        +#+\n" +
                        "#+#        #+#        #+#    #+# #+#    #+# #+#    #+# #+#            #+#     #+#        #+#\n" +
                        "###        ##########  ########   ########  #########  ###        ########### ########## ##########" + ColorType.RESET + "\n";

        System.out.println(art);
        System.out.println("\u001b[38;5;49m" + "                                     Welcome to the FloodFill!                                     " + ColorType.RESET);
        System.out.println("\u001B[38;5;50m" + ":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::" + ColorType.RESET);

        while (true) {
            System.out.print("\n  Enter your username: ");
            userName = scanner.nextLine().trim().toLowerCase();

            if (userName.length() < 10) {
                break;
            } else {
                System.out.println("\n" + "\u001B[38;5;9m" + "Invalid username! The name length must not exceed 9 characters." + ColorType.RESET);
            }
        }


        ConsoleUI console = new ConsoleUI(userName);

        console.renderMenu();
    }
}
