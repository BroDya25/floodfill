package sk.tuke.gamestudio.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.Date;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserController {

    private static final String GAME_NAME = "floodfill";
    public static final String SESSION_USER = "loggedUser";

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String login(HttpSession session) {
        if (session.getAttribute(SESSION_USER) != null) {
            return "redirect:/floodfill";
        }
        return "login";
    }

    @RequestMapping("/login")
    public String loginOrRegister(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        String trimmedUser = username.trim();

        if (trimmedUser.isBlank() || password.isBlank()) {
            model.addAttribute("error", "The username or password cannot be empty.");
            return "login";
        }

        if (trimmedUser.length() > 14) {
            model.addAttribute("error", "The login cannot be longer than 14 characters.");
            return "login";
        }

        boolean success = userService.loginOrRegister(new User(GAME_NAME, username.toLowerCase(), password, new Date()));

        if (success) {
            session.setAttribute(SESSION_USER, trimmedUser);
            return "redirect:/floodfill";
        } else {
            model.addAttribute("error", "Incorrect password. Please try again.");
            model.addAttribute("username", trimmedUser);
            return "login";
        }
    }

    @RequestMapping("/guest")
    public String loginAsGuest(HttpSession session) {
        String guestName = "player_" + java.util.UUID.randomUUID().toString().substring(0, 5);
        session.setAttribute(SESSION_USER, guestName);
        return "redirect:/floodfill";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}