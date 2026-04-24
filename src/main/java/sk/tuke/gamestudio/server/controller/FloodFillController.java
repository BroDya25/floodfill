    package sk.tuke.gamestudio.server.controller;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.annotation.Scope;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.context.WebApplicationContext;
    import sk.tuke.gamestudio.entity.Comment;
    import sk.tuke.gamestudio.entity.Rating;
    import sk.tuke.gamestudio.entity.Score;
    import sk.tuke.gamestudio.game.floodfill.core.ColorType;
    import sk.tuke.gamestudio.game.floodfill.core.Field;
    import sk.tuke.gamestudio.game.floodfill.core.GameState;
    import sk.tuke.gamestudio.service.CommentService;
    import sk.tuke.gamestudio.service.RatingService;
    import sk.tuke.gamestudio.service.ScoreService;

    import java.awt.*;
    import java.util.Date;

    @Controller
    @RequestMapping("/floodfill")
    @Scope(WebApplicationContext.SCOPE_SESSION)
    public class FloodFillController {

        private static final String GAME_NAME = "floodfill";
        private String loggedUser = "Player_" + java.util.UUID.randomUUID().toString().substring(0, 5);
        private static final int FIELD_SIZE = 14;

        private Field field = new Field(FIELD_SIZE, FIELD_SIZE);

        private boolean scoreSaved = false;

        @Autowired
        private ScoreService scoreService;

        @Autowired
        private CommentService commentService;

        @Autowired
        private RatingService ratingService;

        @RequestMapping
        public String floodFill(@RequestParam(required = false) String color, Model model) {
            if (color != null) {
                processColor(color);
            }
            fillModel(model);
            return "floodfill";
        }

        private void processColor(String color) {
            try {
                ColorType newColor = ColorType.valueOf(color.toUpperCase());
                ColorType currentColor = field.getGrid()[0][0].getColor();
                if (newColor != currentColor) {
                    field.floodFill(0, 0, newColor, currentColor);
                    field.setCurrentMoves(field.getCurrentMoves() + 1);
                    field.checkState();

                    if (field.getState() == GameState.SOLVED && !scoreSaved) {
                        scoreService.addScore(new Score(GAME_NAME, loggedUser, field.getScore(), new Date()));
                        scoreSaved = true;
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }

        private void fillModel(Model model) {
            model.addAttribute("field",         field);
            model.addAttribute("gameState",     field.getState().name());
            model.addAttribute("currentMoves",  field.getCurrentMoves());
            model.addAttribute("maxMoves",      field.getMaxMoves());
            model.addAttribute("colors",        ColorType.values());
            model.addAttribute("currentColor",  field.getGrid()[0][0].getColor().name());
            model.addAttribute("topScores",     scoreService.getTopScores(GAME_NAME));
            model.addAttribute("comments",      commentService.getComments(GAME_NAME));
            model.addAttribute("averageRating", ratingService.getAverageRating(GAME_NAME));
            model.addAttribute("userRating", ratingService.getRating(GAME_NAME, loggedUser));
        }

        @RequestMapping("/newGame")
        public String newGame(Model model) {
            field = new Field(FIELD_SIZE, FIELD_SIZE);
            scoreSaved = false;
            fillModel(model);
            return "redirect:/floodfill";
        }

        @RequestMapping("/rating")
        public String rating(@RequestParam int rating, Model model) {
            ratingService.setRating(new Rating(GAME_NAME, loggedUser, rating, new Date()));
            fillModel(model);
            return "fragments/services :: ratingFragment";
        }

        @RequestMapping(value = "/comment", method = RequestMethod.POST)
        public String comment(@RequestParam String comment, Model model) {
            if (comment != null && !comment.trim().isEmpty()) {
                commentService.addComment(new Comment(GAME_NAME, loggedUser, comment.trim(), new Date()));
            }
            fillModel(model);
            return "fragments/services :: commentFragment";
        }

        @RequestMapping("/field")
        public String floodFillAjax(@RequestParam(required = false) String color, Model model) {
            if (color != null) {
                processColor(color);
            }
            fillModel(model);
            return "fragments/field :: gameFragment";
        }
    }