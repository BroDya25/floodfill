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
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.CommentServiceJDBC;
import sk.tuke.gamestudio.service.CommentServiceJPA;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentServiceJDBCTest {

    private CommentServiceJDBC commentService;

    @BeforeEach
    public void setUp() {
        commentService = new CommentServiceJDBC();
        commentService.reset();
    }

    @AfterAll
    public void tearDown() {
        commentService.reset();
    }

    @Test
    public void testAddCommentAndGetComments() {
        Comment comment = new Comment("FloodFill", "Jozef", "Great game!", new Date());

        commentService.addComment(comment);
        List<Comment> comments = commentService.getComments("FloodFill");

        assertNotNull(comments);
        assertEquals(1, comments.size());

        Comment retrievedComment = comments.get(0);
        assertEquals("FloodFill", retrievedComment.getGame());
        assertEquals("Jozef", retrievedComment.getPlayer());
        assertEquals("Great game!", retrievedComment.getComment());

        assertNotNull(retrievedComment.getCommentedOn());
    }

    @Test
    public void testGetCommentsForDifferentGames() throws Exception {
        commentService.addComment(new Comment("FloodFill", "Player1", "Bomb!", new Date()));
        commentService.addComment(new Comment("Tetris", "Player2", "Blocks!", new Date()));

        List<Comment> minesweeperComments = commentService.getComments("FloodFill");

        assertEquals(1, minesweeperComments.size());
        assertEquals("FloodFill", minesweeperComments.get(0).getGame());
        assertEquals("Bomb!", minesweeperComments.get(0).getComment());
    }

    @Test
    public void testGetCommentsLimitsToTen() {
        for (int i = 0; i < 12; i++) {
            commentService.addComment(new Comment("FloodFill", "Player" + i, "Comment " + i, new Date()));
        }

        List<Comment> comments = commentService.getComments("FloodFill");

        assertEquals(10, comments.size(), "The method should return a maximum of 10 comments.");
    }

    @Test
    public void testResetClearsAllData() {
        commentService.addComment(new Comment("FloodFill", "Player1", "Nice", new Date()));

        commentService.reset();

        List<Comment> comments = commentService.getComments("FloodFill");

        assertTrue(comments.isEmpty(), "The table should be empty after reset.");
    }
}

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Transactional
class CommentServiceJPATest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public CommandLineRunner runner() {
            return args -> {};
        }

        @Bean
        public CommentServiceJPA commentServiceJPA() {
            return new CommentServiceJPA();
        }
    }

    @Autowired
    private CommentServiceJPA commentService;

    @BeforeEach
    void setUp() {
        commentService.reset();
    }

    @Test
    public void testAddCommentAndGetComments() {
        Comment comment = new Comment("FloodFill", "Jozef", "Great game!", new Date());

        commentService.addComment(comment);
        List<Comment> comments = commentService.getComments("FloodFill");

        assertNotNull(comments);
        assertEquals(1, comments.size());

        Comment retrievedComment = comments.get(0);
        assertEquals("FloodFill", retrievedComment.getGame());
        assertEquals("Jozef", retrievedComment.getPlayer());
        assertEquals("Great game!", retrievedComment.getComment());
        assertNotNull(retrievedComment.getCommentedOn());
    }

    @Test
    public void testGetCommentsForDifferentGames() {
        commentService.addComment(new Comment("FloodFill", "Player1", "Bomb!", new Date()));
        commentService.addComment(new Comment("Tetris", "Player2", "Blocks!", new Date()));

        List<Comment> floodFillComments = commentService.getComments("FloodFill");

        assertEquals(1, floodFillComments.size());
        assertEquals("FloodFill", floodFillComments.get(0).getGame());
        assertEquals("Bomb!", floodFillComments.get(0).getComment());
    }

    @Test
    public void testGetCommentsLimitsToTen() {
        for (int i = 0; i < 12; i++) {
            commentService.addComment(new Comment("FloodFill", "Player" + i, "Comment " + i, new Date()));
        }

        List<Comment> comments = commentService.getComments("FloodFill");

        assertEquals(10, comments.size(), "The method should return a maximum of 10 comments.");
    }

    @Test
    public void testGetCommentsOrderedByDateDescending() {
        commentService.addComment(new Comment("FloodFill", "Player1", "Old comment", new Date(1_000_000L)));
        commentService.addComment(new Comment("FloodFill", "Player2", "New comment", new Date(9_000_000L)));

        List<Comment> comments = commentService.getComments("FloodFill");

        assertEquals(2, comments.size());
        assertEquals("New comment", comments.get(0).getComment(), "Most recent comment must appear first.");
    }

    @Test
    public void testResetClearsAllData() {
        commentService.addComment(new Comment("FloodFill", "Player1", "Nice", new Date()));

        commentService.reset();

        assertTrue(commentService.getComments("FloodFill").isEmpty(), "The table should be empty after reset.");
    }
}