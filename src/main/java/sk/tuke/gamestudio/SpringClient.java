package sk.tuke.gamestudio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.game.floodfill.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.floodfill.core.ColorType;
import sk.tuke.gamestudio.service.*;

import java.util.Scanner;

@SpringBootApplication
@Configuration
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "sk.tuke.gamestudio.server.*"))
public class SpringClient {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringClient.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public CommandLineRunner runner(ConsoleUI ui) {
        return args -> {
            printWelcomeBanner();
            String userName = promptForUsername();
            ui.setUserName(userName);
            ui.renderMenu();
        };
    }

    private void printWelcomeBanner() {
        String art = "\n" + "\u001B[38;5;48m" +
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
    }

    private String promptForUsername() {
        Scanner scanner = new Scanner(System.in);
        String userName;
        while (true) {
            System.out.print("\n  Enter your username: ");
            userName = scanner.nextLine().trim().toLowerCase();

            if (userName.length() < 10 && !userName.isEmpty()) {
                return userName;
            } else {
                System.out.println("\n" + "\u001B[38;5;9m" + "Invalid username! The name must be 1-9 characters long." + ColorType.RESET);
            }
        }
    }

    @Bean
    public ConsoleUI consoleUI() {
        return new ConsoleUI();
    }

    @Bean
    public ScoreService scoreService() {
        return new ScoreServiceRestClient();
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceRestClient();
    }

    @Bean
    public RatingService ratingService() {
        return new RatingServiceRestClient();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}