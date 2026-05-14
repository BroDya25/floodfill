package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.User;
import sk.tuke.gamestudio.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserServiceRest {

    @Autowired
    private UserService userService;

    @PostMapping
    public boolean loginOrRegister(@RequestBody User user) {
        return userService.loginOrRegister(user);
    }
}