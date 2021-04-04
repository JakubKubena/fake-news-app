package sk.kubena.fakenews.ajax;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleDTO;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.Rating;
import sk.kubena.fakenews.rating.RatingService;
import sk.kubena.fakenews.user.UserDTO;
import sk.kubena.fakenews.user.UserService;

@Controller
public class AJAXController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AJAXController.class);

    private final ArticleService articleService;
    private final RatingService ratingService;
    private final UserService userService;

    @Autowired
    public AJAXController(ArticleService articleService, RatingService ratingService, UserService userService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
        this.userService = userService;
    }

    // TODO: 31/03/2021 handle extension uninstallation
//    @GetMapping(path = "/uninstalled/{tokenString}")
//    public ResponseEntity<?> uninstalledResponseEntity(@PathVariable String tokenString) {
//        if (tokenService.isTokenValid(tokenString)) {
//            tokenService.invalidateToken(tokenString);
//            LOGGER.info("\nExtension '{}' was uninstalled and its token is now invalid.", tokenString);
//        } else {
//            LOGGER.warn("\nToken '{}' is already invalid or doesnt exist.", tokenString);
//        }
//
//        return ResponseEntity.ok().build();
//    }

    // TODO: 31/03/2021  make this method less awful, fix csrf
    // intercepts incoming user authentication requests
    @PostMapping(path = "/authenticate")
    public ResponseEntity<String> authenticateUser(@RequestBody UserDTO userDTO) {
        if (userService.authenticateUser(userDTO) == null) {
            LOGGER.info("Attempted login: '{}' : '{}'", userDTO.getEmail(), userDTO.getPassword());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wrong email or password!");
        } else {
            LOGGER.info("User '{}' has successfully logged in to an extension.", userDTO.getEmail());
            return ResponseEntity.ok(userService.authenticateUser(userDTO));
        }
    }

    // TODO: 01/04/2021 connect article and user through ID instead of token
    // intercepts incoming rating requests
    @PostMapping(path = "/request-ratings", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> ratingResponseEntity(@RequestBody ArticleDTO articleDTO) {
        // check for null
        if(articleDTO == null) {
            LOGGER.warn("Request body is null!");
            return ResponseEntity.badRequest().build();
        } else if(articleDTO.getUrl() == null || articleDTO.getTitle() == null || articleDTO.getToken() == null) {
            LOGGER.warn("Null parameter in request body: {}", articleDTO.toString());
            return ResponseEntity.badRequest().build();
        // check if user is valid
        } else if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("Requested ratings for article: {}", articleDTO.toString());
            JSONObject responseJson = new JSONObject();
            // check if article already exists
            if (articleService.urlExists(articleDTO.getUrl())) {
                Article existingArticle = articleService.getArticleByUrl(articleDTO.getUrl());
                responseJson.put("rating1", ratingService.getRatingCount(existingArticle, "true"));
                responseJson.put("rating2", ratingService.getRatingCount(existingArticle, "false"));
                responseJson.put("rating3", ratingService.getRatingCount(existingArticle, "misleading"));
                responseJson.put("rating4", ratingService.getRatingCount(existingArticle, "unverified"));
                LOGGER.info("Record of article found - sending ratings: {}", responseJson);
            // if not, send 0 ratings
            } else {
                responseJson.put("rating1", 0);
                responseJson.put("rating2", 0);
                responseJson.put("rating3", 0);
                responseJson.put("rating4", 0);
                LOGGER.info("No record of article found - sending 0 ratings: {}", responseJson);
            }
            return ResponseEntity.ok().header("Content-Type", "application/json").body(responseJson.toString());
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());
            return ResponseEntity.badRequest().build();
        }
    }

    // TODO: 01/04/2021 check if rating value is 1 of the 4
    // intercepts incoming article requests
    @PostMapping(path = "/api", consumes = "application/json")
    public ResponseEntity<?> articleResponseEntity(@RequestBody ArticleDTO articleDTO) {
        // check for null
        if(articleDTO == null) {
            LOGGER.warn("Request body is null!");
            return ResponseEntity.badRequest().build();
        } else if(articleDTO.getUrl() == null || articleDTO.getHostname() == null || articleDTO.getTitle() == null ||
                articleDTO.getUserRating() == null || articleDTO.getContent() == null || articleDTO.getToken() == null) {
            LOGGER.warn("Null parameter in request body: {}", articleDTO.toString());
            return ResponseEntity.badRequest().build();
        // check if user is valid
        } else if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("token {} is valid.", articleDTO.getToken());
            LOGGER.info("Incoming article: {}", articleDTO.toString());
            // check if article already exists
            if (!articleService.urlExists(articleDTO.getUrl())) {
                // add new article
                articleService.addArticle(articleDTO);
                LOGGER.info("New article: {}", articleDTO.toString());
                // add new rating
                ratingService.addRating(new Rating(articleDTO.getUserRating(), articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())));
                LOGGER.info("New rating: {}", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).toString());
                return ResponseEntity.ok().build();
            // if article exists, check if rating exists
            } else if(ratingService.ratingExists(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()))) {
                LOGGER.warn("Rating of {} by {} already exists!", articleDTO.toString(), userService.getUserByToken(articleDTO.getToken()).toString());
                return ResponseEntity.badRequest().body("You already rated this article.");
            // if not, add new rating
            } else {
                ratingService.addRating(new Rating(articleDTO.getUserRating(), articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())));
                LOGGER.info("New rating: {}", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).toString());
                return ResponseEntity.ok().build();
            }
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());
            return ResponseEntity.badRequest().build();
        }
    }

    // home
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
    }

    @GetMapping(path = "/login")
    public String login() {
        return "views/login";
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> getFile() {
        String filename = "articles.csv";
        InputStreamResource file = new InputStreamResource(articleService.load());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @GetMapping("/ratings")
    public String ratings(Model model) {
        model.addAttribute("ratings", ratingService.getAllRatings());

        return "views/ratings";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());

        return "views/users";
    }

    @GetMapping("/article/{id}")
    public String article(Model model, @PathVariable int id) {
        model.addAttribute("article", articleService.getArticleById(id));

        return "views/articleDetails";
    }
}
