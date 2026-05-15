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
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.RatingServiceJDBC;
import sk.tuke.gamestudio.service.RatingServiceJPA;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RatingServiceJDBCTest {

    private RatingServiceJDBC ratingService;

    @BeforeEach
    public void setUp() {
        ratingService = new RatingServiceJDBC();
        ratingService.reset();
    }

    @AfterAll
    public void tearDown() {
        ratingService.reset();
    }

    @Test
    public void testSetAndGetSpecificRating() {
        Rating rating = new Rating("FloodFill", "Peter", 5, new Date());

        ratingService.setRating(rating);
        double retrievedRating = ratingService.getRating("FloodFill", "Peter");

        assertEquals(5.0, retrievedRating, "The rating must match the established one");
    }

    @Test
    public void testGetAverageRating() {
        ratingService.setRating(new Rating("FloodFill", "Player1", 3, new Date()));
        ratingService.setRating(new Rating("FloodFill", "Player2", 4, new Date()));
        ratingService.setRating(new Rating("FloodFill", "Player3", 5, new Date()));

        double average = ratingService.getAverageRating("FloodFill");

        assertEquals(4.0, average, 0.001);
    }

    @Test
    public void testGetRatingReturnsZeroIfNotFound() throws Exception {
        double rating = ratingService.getRating("non_existent_game", "NoPlayer");

        assertEquals(0.0, rating, "If there is no rating, 0 should be returned.");
    }

    @Test
    public void testGetAverageRatingForEmptyGame() {
        double average = ratingService.getAverageRating("empty_game");

        assertTrue(Double.isNaN(average), "The average for an empty game in the current implementation returns NaN");
    }

    @Test
    public void testReset() {
        ratingService.setRating(new Rating("FloodFill", "Player1", 5, new Date()));

        ratingService.reset();

        assertEquals(0, ratingService.getRating("FloodFill", "Player1"));
    }
}

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Transactional
class RatingServiceJPATest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public CommandLineRunner runner() {
            return args -> {};
        }

        @Bean
        public RatingServiceJPA ratingServiceJPA() {
            return new RatingServiceJPA();
        }
    }

    @Autowired
    private RatingServiceJPA ratingService;

    @BeforeEach
    void setUp() {
        ratingService.reset();
    }

    @Test
    public void testSetAndGetSpecificRating() {
        ratingService.setRating(new Rating("FloodFill", "Peter", 5, new Date()));

        double retrievedRating = ratingService.getRating("FloodFill", "Peter");

        assertEquals(5.0, retrievedRating, "The rating must match the established one.");
    }

    @Test
    public void testGetAverageRating() {
        ratingService.setRating(new Rating("FloodFill", "Player1", 3, new Date()));
        ratingService.setRating(new Rating("FloodFill", "Player2", 4, new Date()));
        ratingService.setRating(new Rating("FloodFill", "Player3", 5, new Date()));

        double average = ratingService.getAverageRating("FloodFill");

        assertEquals(4.0, average, 0.001);
    }

    @Test
    public void testGetRatingReturnsZeroIfNotFound() {
        double rating = ratingService.getRating("non_existent_game", "NoPlayer");

        assertEquals(0.0, rating, "If there is no rating, 0 should be returned.");
    }

    @Test
    public void testGetAverageRatingReturnsZeroForEmptyGame() {
        double average = ratingService.getAverageRating("empty_game");

        assertEquals(0.0, average, 0.001, "Average for a game with no ratings must be 0.");
    }

    @Test
    public void testSetRatingUpdatesExistingEntry() {
        ratingService.setRating(new Rating("FloodFill", "Peter", 3, new Date()));
        ratingService.setRating(new Rating("FloodFill", "Peter", 5, new Date()));

        double result = ratingService.getRating("FloodFill", "Peter");

        assertEquals(5.0, result, 0.001, "Existing rating must be updated, not duplicated.");
    }

    @Test
    public void testGetAverageRatingConsidersOnlyRequestedGame() {
        ratingService.setRating(new Rating("FloodFill", "Player1", 5, new Date()));
        ratingService.setRating(new Rating("Tetris", "Player2", 1, new Date()));

        double average = ratingService.getAverageRating("FloodFill");

        assertEquals(5.0, average, 0.001, "Average must not include ratings from other games.");
    }

    @Test
    public void testReset() {
        ratingService.setRating(new Rating("FloodFill", "Player1", 5, new Date()));

        ratingService.reset();

        assertEquals(0.0, ratingService.getRating("FloodFill", "Player1"), 0.001);
        assertEquals(0.0, ratingService.getAverageRating("FloodFill"), 0.001);
    }
}