package sk.tuke.gamestudio.game.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.ScoreServiceJDBC;
import sk.tuke.gamestudio.service.ScoreServiceJPA;

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

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Transactional
class ScoreServiceJPATest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public CommandLineRunner runner() {
            return args -> {};
        }

        @Bean
        public ScoreServiceJPA scoreServiceJPA() {
            return new ScoreServiceJPA();
        }
    }

    @Autowired
    private ScoreServiceJPA scoreService;

    @BeforeEach
    void setUp() {
        scoreService.reset();
    }

    @Test
    void testAddScoreAndGetTopScores() {
        scoreService.addScore(new Score("FloodFill", "Jozef", 150, new Date()));
        List<Score> topScores = scoreService.getTopScores("FloodFill");

        assertNotNull(topScores);
        assertEquals(1, topScores.size());

        Score retrieved = topScores.get(0);
        assertEquals("FloodFill", retrieved.getGame());
        assertEquals("Jozef", retrieved.getPlayer());
        assertEquals(150, retrieved.getPoints());
        assertNotNull(retrieved.getPlayedOn());
    }

    @Test
    void testGetTopScoresOrdersByPointsDesc() {
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
    void testGetTopScoresForDifferentGames() {
        scoreService.addScore(new Score("FloodFill", "Player1", 100, new Date()));
        scoreService.addScore(new Score("Tetris",    "Player2", 200, new Date()));

        List<Score> floodFillScores = scoreService.getTopScores("FloodFill");

        assertEquals(1, floodFillScores.size());
        assertEquals("FloodFill", floodFillScores.get(0).getGame());
    }

    @Test
    void testGetTopScoresLimitsToTen() {
        for (int i = 0; i < 15; i++) {
            scoreService.addScore(new Score("FloodFill", "Player" + i, 100 + i, new Date()));
        }

        List<Score> topScores = scoreService.getTopScores("FloodFill");

        assertEquals(10, topScores.size(), "Метод должен возвращать не более 10 результатов.");
    }

    @Test
    void testResetClearsAllData() {
        scoreService.addScore(new Score("FloodFill", "Player1", 100, new Date()));
        scoreService.reset();

        assertTrue(scoreService.getTopScores("FloodFill").isEmpty(),
                "После reset() таблица должна быть пустой.");
    }
}