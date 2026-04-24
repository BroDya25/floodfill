package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import sk.tuke.gamestudio.entity.Rating;

import java.util.List;

@Transactional
public class RatingServiceJPA implements RatingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(Rating rating) throws RatingException {
        List<Rating> existingRatings = entityManager.createNamedQuery("Rating.getRating", Rating.class).setParameter("game", rating.getGame()).setParameter("player", rating.getPlayer()).getResultList();

        if (!existingRatings.isEmpty()) {
            Rating existingRating = existingRatings.get(0);
            existingRating.setRating(rating.getRating());
            existingRating.setRatedOn(rating.getRatedOn());
        } else {
            entityManager.persist(rating);
        }
    }

    @Override
    public double getAverageRating(String game) throws RatingException {
        Double result = entityManager.createNamedQuery("Rating.getAverageRating", Double.class).setParameter("game", game).getSingleResult();
        return result == null ? 0 : result;
    }

    @Override
    public double getRating(String game, String player) throws RatingException {
        List<Rating> result = entityManager.createNamedQuery("Rating.getRating", Rating.class).setParameter("game", game).setParameter("player", player).getResultList();
        return result.isEmpty() ? 0 : result.get(0).getRating();
    }

    @Override
    public void reset() throws RatingException {
        entityManager.createNamedQuery("Rating.reset").executeUpdate();
    }
}
