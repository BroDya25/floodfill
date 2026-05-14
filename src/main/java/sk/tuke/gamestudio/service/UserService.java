package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.User;

public interface UserService {
    boolean loginOrRegister(User user);
    void reset() throws RatingException;
}
