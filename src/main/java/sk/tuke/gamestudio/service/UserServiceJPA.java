package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import sk.tuke.gamestudio.entity.User;

import java.util.List;

@Transactional
public class UserServiceJPA implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean loginOrRegister(User user) throws UserException {
        List<User> existingUsers = entityManager.createNamedQuery("User.getUser", User.class).setParameter("game", user.getGame()).setParameter("username", user.getUsername()).getResultList();

        if (!existingUsers.isEmpty()) {
            return existingUsers.get(0).getPassword().equals(user.getPassword());
        } else {
            entityManager.persist(user);
            return true;
        }
    }

    @Override
    public void reset() {
        entityManager.createNamedQuery("User.reset").executeUpdate();
    }
}
