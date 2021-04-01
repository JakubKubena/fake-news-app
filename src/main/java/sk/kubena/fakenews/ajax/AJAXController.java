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
    @PostMapping(path = "/authenticate")
    public ResponseEntity<String> authenticateUser(@RequestBody UserDTO userDTO) {
        LOGGER.info(userDTO.toString());
        if (userService.authenticateUser(userDTO) == null) {
            LOGGER.info("Attempted login: '{}' : '{}'", userDTO.getEmail(), userDTO.getPassword());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wrong email or password!");
        } else {
            LOGGER.info("User '{}' has successfully logged in to an extension.", userDTO.getEmail());
            return ResponseEntity.ok(userService.authenticateUser(userDTO));
        }
    }

    // TODO: 01/04/2021 connect article and user through ID instead of token
    @PostMapping(path = "/request-ratings", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> ratingResponseEntity(@RequestBody ArticleDTO articleDTO) {
        JSONObject responseJson;
        if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("Requested ratings for article: {}", articleDTO.toString());

            Article existingArticle = articleService.checkIfArticleAlreadyExists(articleDTO);

            responseJson = new JSONObject();

            if (existingArticle == null) {
                responseJson.put("rating1", 0);
                responseJson.put("rating2", 0);
                responseJson.put("rating3", 0);
                responseJson.put("rating4", 0);
                LOGGER.info("No record of article found - sending 0 ratings: {}", responseJson);
            } else {
                LOGGER.info("Found a record: {}", existingArticle.toString());
                Rating rating = ratingService.getRatingsOfArticle(existingArticle);
                responseJson.put("rating1", rating.getRating1());
                responseJson.put("rating2", rating.getRating2());
                responseJson.put("rating3", rating.getRating3());
                responseJson.put("rating4", rating.getRating4());
                LOGGER.info("Record of article found - sending ratings: {}", responseJson);
            }

            return ResponseEntity.ok().header("Content-Type", "application/json").body(responseJson.toString());
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());

            return ResponseEntity.ok().build();
        }
    }

    // TODO: 01/04/2021 check if rating value is 1 of the 4
    @PostMapping(path = "/api", consumes = "application/json")
    public ResponseEntity<?> articleResponseEntity(@RequestBody ArticleDTO articleDTO) {
        if (userService.tokenExists(articleDTO.getToken())) {
            Article existingArticle = articleService.checkIfArticleAlreadyExists(articleDTO);

            if (existingArticle == null) {
                switch (articleDTO.getUserRating()) {
                    case "true":
                        articleService.addArticle(articleDTO);
                        ratingService.addRating(new Rating(1, 0, 0, 0, articleService.getArticle(articleDTO.getUrl())));
                        break;
                    case "false":
                        articleService.addArticle(articleDTO);
                        ratingService.addRating(new Rating(0, 1, 0, 0, articleService.getArticle(articleDTO.getUrl())));
                        break;
                    case "misleading":
                        articleService.addArticle(articleDTO);
                        ratingService.addRating(new Rating(0, 0, 1, 0, articleService.getArticle(articleDTO.getUrl())));
                        break;
                    case "unverified":
                        articleService.addArticle(articleDTO);
                        ratingService.addRating(new Rating(0, 0, 0, 1, articleService.getArticle(articleDTO.getUrl())));
                        break;
                }

                LOGGER.info("New article: {}", articleDTO.toString());
            } else {
                LOGGER.info("New article rating: {}", articleDTO.toString());
                ratingService.incrementRating(articleDTO);
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
    }

    @GetMapping("/login")
    public String login(Model model) {
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
}
