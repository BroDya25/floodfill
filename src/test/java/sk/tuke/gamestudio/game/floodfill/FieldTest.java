package sk.tuke.gamestudio.game.floodfill;

import org.junit.jupiter.api.Test;

import static java.lang.Math.round;
import static org.junit.jupiter.api.Assertions.*;

import sk.tuke.gamestudio.game.floodfill.core.Cell;
import sk.tuke.gamestudio.game.floodfill.core.ColorType;
import sk.tuke.gamestudio.game.floodfill.core.Field;
import sk.tuke.gamestudio.game.floodfill.core.GameState;

import java.util.Random;

public class FieldTest {
    private final int rowCount;
    private final int columnCount;
    private final Field field;

    public FieldTest() {
        Random random = new Random();
        rowCount = random.nextInt(10) + 12;
        columnCount = rowCount;
        field = new Field(rowCount, columnCount);
    }

    // Test Constructor //

    @Test
    public void fieldShouldThrowExceptionForInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Field(11, 11),
                "There must be an exception: size smaller than 12");
        assertThrows(IllegalArgumentException.class, () -> new Field(23, 23),
                "There must be an exception: size higher than 22");
    }

    @Test
    public void fieldShouldBeCreatedWhenDimensionsAreCorrect() {
        assertTrue(rowCount == field.getRowCount() || columnCount == field.getColumnCount(), "In the created field, the row count or the column count has a different value than in the argument");
    }

    // Test Generate //

    @Test
    public void checkFieldForNull() {
        field.generate();
        assertNotNull(field.getGrid(), "The field should not be null");
    }

    @Test
    public void checkFieldForNullCells() {
        field.generate();

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                assertNotNull(field.getGrid()[i][j], "Some cells of the array field have null values");
            }
        }
    }

    // Test FloodFill //

    @Test
    public void floodFillInvalidCoordinatesShouldNotThrow() {
        field.generate();

        int[][] invalidCoordinates = {
                {-1, 0},
                {0, -1},
                {rowCount, 0},
                {0, columnCount}
        };

        for (int[] args : invalidCoordinates) {
            assertDoesNotThrow(() -> field.floodFill(args[0], args[1], ColorType.RED, field.getGrid()[0][0].getColor()));
        }
    }

    @Test
    public void floodFillNullColorShouldNotChangeField() {
        field.generate();
        ColorType currentColor = field.getGrid()[0][0].getColor();
        field.floodFill(0, 0, null, field.getGrid()[0][0].getColor());

        assertEquals(currentColor, field.getGrid()[0][0].getColor());
    }

    // Test GameState //

    @Test
    public void checkStateForPLAYING() {
        field.generate();
        field.checkState();
        assertEquals(GameState.PLAYING, field.getState(), "Game state is not PLAYING");
    }

    @Test
    public void checkStateForFAILED() {
        field.generate();
        field.setCurrentMoves(field.getMaxMoves());
        field.checkState();
        assertEquals(GameState.FAILED, field.getState(), "Game state is not FAILED");
    }

    @Test
    public void checkStateForSOLVED() {
        Cell[][] grid = field.getGrid();

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                grid[i][j] = new Cell(i, j, ColorType.RED);
            }
        }

        field.checkState();

        assertEquals(GameState.SOLVED, field.getState(), "Game state is not SOLVED");
    }
}
