package sk.tuke.gamestudio.game.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.ScoreServiceJDBC;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ScoreServiceJDBCTest {

    private ScoreServiceJDBC scoreService;

    @BeforeEach
    public void setUp() {
        scoreService = new ScoreServiceJDBC();
        scoreService.reset();
    }

    @AfterAll
    public void tearDown() {
        scoreService.reset();
    }

    @Test
    public void testAddScoreAndGetTopScores() {
        scoreService.addScore(new Score("FloodFill", "Jozef", 150, new Date()));
        List<Score> topScores = scoreService.getTopScores("FloodFill");

        assertNotNull(topScores);
        assertEquals(1, topScores.size());

        Score retrievedScore = topScores.get(0);
        assertEquals("FloodFill", retrievedScore.getGame());
        assertEquals("Jozef", retrievedScore.getPlayer());
        assertEquals(150, retrievedScore.getPoints());

        assertNotNull(retrievedScore.getPlayedOn());
    }

    @Test
    public void testGetTopScoresOrdersByPointsDesc() {
        scoreService.addScore(new Score("FloodFill", "Player1", 100, new Date()));
        scoreService.addScore(new Score("FloodFill", "Player2", 300, new Date()));
        scoreService.addScore(new Score("FloodFill", "Player3", 200, new Date()));

        List<Score> topScores = scoreService.getTopScores("FloodFill");

        assertEquals(3, topScores.size());
        assertEquals(300, topScores.get(0).getPoints());
        assertEquals(200, topScores.get(1).getPoints());
        assertEquals(100, topScores.get(2).getPoints());
    }

    @Test
    public void testGetTopScoresForDifferentGames() {
        scoreService.addScore(new Score("FloodFill", "Player1", 100, new Date()));
        scoreService.addScore(new Score("Tetris", "Player2", 200, new Date()));

        List<Score> minesweeperScores = scoreService.getTopScores("FloodFill");

        assertEquals(1, minesweeperScores.size());
        assertEquals("FloodFill", minesweeperScores.get(0).getGame());
    }

    @Test
    void testGetTopScoresLimitsToTen() {
        for (int i = 0; i < 15; i++) {
            scoreService.addScore(new Score("FloodFill", "Player" + i, 100 + i, new Date()));
        }

        List<Score> topScores = scoreService.getTopScores("FloodFill");

        assertEquals(10, topScores.size(), "The method should return a maximum of 10 results.");
    }

    @Test
    public void testResetClearsAllData() {
        scoreService.addScore(new Score("FloodFill", "Player1", 100, new Date()));
        scoreService.addScore(new Score("Tetris", "Player2", 200, new Date()));

        scoreService.reset();

        List<Score> minesweeperScores = scoreService.getTopScores("FloodFill");

        assertTrue(minesweeperScores.isEmpty(), "The table should be empty after reset.");
    }
}
