    package sk.tuke.gamestudio.server.controller;

    import jakarta.servlet.http.HttpSession;
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
        private String loggedUser;
        private int FIELD_SIZE = 14;
        private Field field;
        private boolean scoreSaved = false;

        @Autowired
        private ScoreService scoreService;

        @Autowired
        private CommentService commentService;

        @Autowired
        private RatingService ratingService;

        @RequestMapping
        public String floodFill(HttpSession session, Model model) {
            if (!initUser(session)) return "redirect:/";
            if (field == null) field = new Field(FIELD_SIZE, FIELD_SIZE);
            fillModel(model);
            return "floodfill";
        }

        @RequestMapping("/field")
        public String floodFillAjax(@RequestParam(required = false) String color, Model model) {
            if (color != null) processColor(color);
            fillModel(model);
            return "fragments/field :: gameFragment";
        }

        @RequestMapping("/undo")
        public String undo(Model model) {
            if (field != null) field.undo();
            fillModel(model);
            return "fragments/field :: gameFragment";
        }

        @RequestMapping("/redo")
        public String redo(Model model) {
            if (field != null) field.redo();
            fillModel(model);
            return "fragments/field :: gameFragment";
        }

        @RequestMapping("/newGame")
        public String newGame(Model model) {
            field = new Field(FIELD_SIZE, FIELD_SIZE);
            scoreSaved = false;
            fillModel(model);
            return "fragments/field :: gameFragment";
        }

        @RequestMapping("/restartGame")
        public String restartGame(Model model) {
            if (field != null) field.reset();
            scoreSaved = false;
            fillModel(model);
            return "fragments/field :: gameFragment";
        }

        @RequestMapping("/type")
        public String type(@RequestParam int type, Model model) {
            if (type <= 20 && type >= 12) FIELD_SIZE = type;
            return newGame(model);
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

        private boolean initUser(HttpSession session) {
            String u = (String) session.getAttribute(UserController.SESSION_USER);
            if (u == null) return false;
            loggedUser = u.toLowerCase();
            return true;
        }

        private void processColor(String color) {
            if (field == null || field.getState() != GameState.PLAYING) return;
            try {
                ColorType newColor = ColorType.valueOf(color.toUpperCase());
                ColorType currentColor = field.getGrid()[0][0].getColor();
                if (newColor != currentColor) {
                    field.saveSnapshot();
                    field.floodFill(0, 0, newColor, currentColor);
                    field.setCurrentMoves(field.getCurrentMoves() + 1);
                    field.checkState();

                    if (field.getState() == GameState.PLAYING && !scoreSaved) {
                        boolean isGuest = loggedUser != null && loggedUser.startsWith("player_");
                        if (!isGuest) {
                            scoreService.addScore(new Score(GAME_NAME, loggedUser, field.getScore(), new Date()));
                        }
                        scoreSaved = true;
                    }
                }
            } catch (IllegalArgumentException ignored) {}
        }

        private void fillModel(Model model) {
            model.addAttribute("canUndo", false);
            model.addAttribute("canRedo", false);
            if (field == null) return;
            model.addAttribute("field",         field);
            model.addAttribute("size",          FIELD_SIZE);
            boolean isGuest = loggedUser != null && loggedUser.startsWith("player_");
            model.addAttribute("loggedUser",    loggedUser);
            model.addAttribute("isGuest",       isGuest);
            model.addAttribute("gameState",     field.getState().name());
            model.addAttribute("currentMoves",  field.getCurrentMoves());
            model.addAttribute("maxMoves",      field.getMaxMoves());
            model.addAttribute("colors",        ColorType.values());
            model.addAttribute("currentColor",  field.getGrid()[0][0].getColor().name());
            model.addAttribute("canUndo",       field != null && field.canUndo());
            model.addAttribute("canRedo",       field != null && field.canRedo());
            model.addAttribute("topScores",     scoreService.getTopScores(GAME_NAME));
            model.addAttribute("comments",      commentService.getComments(GAME_NAME));
            model.addAttribute("averageRating", ratingService.getAverageRating(GAME_NAME));
            model.addAttribute("userRating",    ratingService.getRating(GAME_NAME, loggedUser));
        }
    }