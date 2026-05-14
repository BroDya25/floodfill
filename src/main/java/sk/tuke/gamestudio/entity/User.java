package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "users")
@NamedQuery( name = "User.getUser", query = "SELECT r FROM User r WHERE r.game=:game AND LOWER(r.username)=LOWER(:username)")
@NamedQuery( name = "User.reset", query = "DELETE FROM User")
public class User implements Serializable {
    @Id
    @GeneratedValue
    private int ident;

    private String game;
    private String username;
    private String password;
    private Date loggedOn;

    public User() {}

    public User(String game, String username, String password, Date loggedOn) {
        this.game = game;
        this.username = username;
        this.password = password;
        this.loggedOn = loggedOn;
    }

    public int getIdent() { return ident; }
    public void setIdent(int ident) { this.ident = ident; }

    public String getGame() {
        return game;
    }
    public void setGame(String game) {
        this.game = game;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLoggedOn() {
        return loggedOn;
    }
    public void setLoggedOn(Date loggedOn) {
        this.loggedOn = loggedOn;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "ident=" + ident +
                "game='" + game + '\'' +
                ", username='" + username + '\'' +
                ", password=" + password +
                ", loggedOn=" + loggedOn +
                '}';
    }
}