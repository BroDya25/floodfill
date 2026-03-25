package sk.tuke.gamestudio.game.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.CommentServiceJDBC;

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
    public void testGetCommentsOrdersByCommentDesc() {
        commentService.addComment(new Comment("FloodFill", "Player1", "A - Bad", new Date()));
        commentService.addComment(new Comment("FloodFill", "Player2", "Z - Excellent", new Date()));
        commentService.addComment(new Comment("FloodFill", "Player3", "M - Average", new Date()));

        List<Comment> comments = commentService.getComments("FloodFill");

        assertEquals(3, comments.size());
        assertEquals("Z - Excellent", comments.get(0).getComment());
        assertEquals("M - Average", comments.get(1).getComment());
        assertEquals("A - Bad", comments.get(2).getComment());
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