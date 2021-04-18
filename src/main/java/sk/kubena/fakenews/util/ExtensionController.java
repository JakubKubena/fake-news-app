package sk.kubena.fakenews.util;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleDTO;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.Rating;
import sk.kubena.fakenews.rating.RatingService;
import sk.kubena.fakenews.user.UserDTO;
import sk.kubena.fakenews.user.UserService;

@Controller
public class ExtensionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionController.class);

    private final ArticleService articleService;
    private final RatingService ratingService;
    private final UserService userService;

    @Autowired
    public ExtensionController(ArticleService articleService, RatingService ratingService, UserService userService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
        this.userService = userService;
    }

    // TODO: 14/04/2021 if we want to create users inside the app
//    @PostMapping(path = "/user")
//    public ResponseEntity<String> addUser(@RequestBody UserDTO userDTO) {
//        if (userDTO == null) {
//            LOGGER.info("Request body is null!");
//            return ResponseEntity.badRequest().body("Null request!");
//        } else {
//            userService.addUser(userDTO);
//            LOGGER.info("User '{}' has been successfully created.", userDTO.getEmail());
//            return ResponseEntity.ok(userService.authenticateUser(userDTO));
//        }
//    }

    // TODO: 31/03/2021 handle extension uninstallation if needed
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

    // intercepts incoming user authentication requests
    @PostMapping(path = "/api/authenticate")
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
    @PostMapping(path = "/api/request-rating", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> ratingResponseEntity(@RequestBody ArticleDTO articleDTO) {

        // check if the request body is null
        if(articleDTO == null) {
            LOGGER.warn("Request body is null!");
            return ResponseEntity.badRequest().build();

        // check if the request body parameters are null
        } else if(articleDTO.getUrl() == null || articleDTO.getTitle() == null || articleDTO.getToken() == null) {
            LOGGER.warn("Null parameter in request body: {}", articleDTO.toString());
            return ResponseEntity.badRequest().build();

        // check if the user token is valid
        } else if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("Requested ratings for article: {}", articleDTO.toString());
            Article existingArticle = articleService.getArticleByUrl(articleDTO.getUrl());
            JSONObject responseJson = new JSONObject();

            // check if the article does not exist
            if (!articleService.urlExists(articleDTO.getUrl())) {
                // return 0 ratings
                responseJson.put("rating1", 0);
                responseJson.put("rating2", 0);
                responseJson.put("rating3", 0);
                responseJson.put("rating4", 0);
                responseJson.put("userRating", "null");
                LOGGER.info("No record of article found - sending 0 ratings.");

            // if the article does exist, then check if the rating of that article by that user does also exist
            } else if(ratingService.ratingExists(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()))) {
                // sending the article ratings along with the article rating by that user
                responseJson.put("rating1", ratingService.getArticleRatingCount(existingArticle, "true"));
                responseJson.put("rating2", ratingService.getArticleRatingCount(existingArticle, "false"));
                responseJson.put("rating3", ratingService.getArticleRatingCount(existingArticle, "misleading"));
                responseJson.put("rating4", ratingService.getArticleRatingCount(existingArticle, "unverified"));
                responseJson.put("userRating", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).getValue());
                LOGGER.warn("Rating of {} by {} found - sending ratings: {}", articleDTO.toString(), userService.getUserByToken(articleDTO.getToken()).toString(), responseJson);

            // if the article rating by that user does not exist, add new rating
            } else {
                responseJson.put("rating1", ratingService.getArticleRatingCount(existingArticle, "true"));
                responseJson.put("rating2", ratingService.getArticleRatingCount(existingArticle, "false"));
                responseJson.put("rating3", ratingService.getArticleRatingCount(existingArticle, "misleading"));
                responseJson.put("rating4", ratingService.getArticleRatingCount(existingArticle, "unverified"));
                responseJson.put("userRating", "null");
                LOGGER.info("Record of article found - sending ratings: {}", responseJson);
            }
            return ResponseEntity.ok().header("Content-Type", "application/json").body(responseJson.toString());

        // if user token is not valid
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());
            return ResponseEntity.badRequest().build();
        }
    }

    // TODO: 01/04/2021 check if rating value is 1 of the 4
    // intercepts the incoming article requests
    @PostMapping(path = "/api/send-rating", consumes = "application/json")
    public ResponseEntity<?> articleResponseEntity(@RequestBody ArticleDTO articleDTO) {
        // check if the request body is null
        if(articleDTO == null) {
            LOGGER.warn("Request body is null!");
            return ResponseEntity.badRequest().build();

        // check if the request body parameters are null
        } else if(articleDTO.getUrl() == null || articleDTO.getHostname() == null || articleDTO.getTitle() == null ||
                articleDTO.getUserRating() == null || articleDTO.getContent() == null || articleDTO.getToken() == null) {
            LOGGER.warn("Null parameter in request body: {}", articleDTO.toString());
            return ResponseEntity.badRequest().build();

        // check if the user token is valid
        } else if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("token {} is valid.", articleDTO.getToken());
            LOGGER.info("Incoming article: {}", articleDTO.toString());

            // check if the article does not exist
            if (!articleService.urlExists(articleDTO.getUrl())) {
                // add new article
                articleService.addArticle(articleDTO);
                LOGGER.info("New article: {}", articleDTO.toString());

                // add new rating
                ratingService.addRating(new Rating(articleDTO.getUserRating(), articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())));
                LOGGER.info("New rating: {}", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).toString());
                return ResponseEntity.ok().build();

            // if the article does exist, then check if the rating of that article by that user does also exist
            } else if(ratingService.ratingExists(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()))) {
                LOGGER.warn("Rating of {} by {} already exists.", articleDTO.toString(), userService.getUserByToken(articleDTO.getToken()).toString());
                Rating rating = ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()));
                rating.setValue(articleDTO.getUserRating());
                ratingService.updateRating(rating);
                return ResponseEntity.ok().build();

            // if the article rating by that user does not exist, add new rating
            } else {
                ratingService.addRating(new Rating(articleDTO.getUserRating(), articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())));
                LOGGER.info("New rating: {}", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).toString());
                return ResponseEntity.ok().build();
            }

        // if user token is not valid
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());
            return ResponseEntity.badRequest().build();
        }
    }
}
