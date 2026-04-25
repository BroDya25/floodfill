package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import sk.tuke.gamestudio.entity.Score;

import java.util.List;

@Transactional
public class ScoreServiceJPA implements ScoreService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addScore(Score score) throws ScoreException {
        List<Score> existingScores = entityManager.createNamedQuery("Score.getScore", Score.class).setParameter("game", score.getGame()).setParameter("player", score.getPlayer()).getResultList();

        if (!existingScores.isEmpty()) {
            if (existingScores.get(0).getPoints() <= score.getPoints()) {
                Score existingScore = existingScores.get(0);
                existingScore.setPoints(score.getPoints());
                existingScore.setPlayedOn(score.getPlayedOn());
            }
        } else {
            entityManager.persist(score);
        }
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException {
        return entityManager.createNamedQuery("Score.getTopScores", Score.class).setParameter("game", game).getResultList();
    }

    @Override
    public void reset() {
        entityManager.createNamedQuery("Score.reset").executeUpdate();
    }
}