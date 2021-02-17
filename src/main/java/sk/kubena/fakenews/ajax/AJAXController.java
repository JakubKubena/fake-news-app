package sk.kubena.fakenews.ajax;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.Rating;
import sk.kubena.fakenews.rating.RatingService;
import sk.kubena.fakenews.token.Token;
import sk.kubena.fakenews.token.TokenGenerator;
import sk.kubena.fakenews.token.TokenService;

@Controller
public class AJAXController {

    private static final Logger logger = LoggerFactory.getLogger(AJAXController.class);

    private final ArticleService articleService;
    private final RatingService ratingService;
    private final TokenService tokenService;

    @Autowired
    public AJAXController(ArticleService articleService, RatingService ratingService, TokenService tokenService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
        this.tokenService = tokenService;
    }

    @GetMapping(path = "/uninstalled/{tokenString}")
    public ResponseEntity<?> uninstalledResponseEntity(@PathVariable String tokenString) {
        if (tokenService.isTokenValid(tokenString)) {
            tokenService.invalidateToken(tokenString);
            logger.info("\nExtension '{}' was uninstalled and its token is now invalid.", tokenString);
        } else {
            logger.warn("\nToken '{}' is already invalid or doesnt exist.", tokenString);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/request-token")
    public ResponseEntity<String> tokenResponseEntity() {
        String tokenString = TokenGenerator.generateType1UUID().toString();
        tokenService.addToken(new Token(tokenString, true));
        logger.info("\nExtension was installed and a new token '{}' was generated.", tokenString);

        return ResponseEntity.ok(tokenString);
    }

    @PostMapping(path = "/request-ratings", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> ratingResponseEntity(@RequestBody Article article) {
        JSONObject responseJson;
        if (tokenService.isTokenValid(article.getToken())) {
            logger.info("\nReceived rating request article: \nURL: {}\nTitle: {}\nToken: {}\n", article.getUrl(), article.getTitle(), article.getToken());

            Article existingArticle = articleService.checkIfArticleAlreadyExists(article);

            responseJson = new JSONObject();

            if (existingArticle == null) {
                responseJson.put("rating1", 0);
                responseJson.put("rating2", 0);
                responseJson.put("rating3", 0);
                responseJson.put("rating4", 0);
                logger.info("\nNo record of article - sending 0 ratings: {}", responseJson);
            } else {
                logger.info("\nFound a record of the received article: \n{}\n", existingArticle);
                Rating rating = ratingService.getRatingsOfArticle(existingArticle);
                responseJson.put("rating1", rating.getRating1());
                responseJson.put("rating2", rating.getRating2());
                responseJson.put("rating3", rating.getRating3());
                responseJson.put("rating4", rating.getRating4());
                logger.info("\nRecord of article found - sending ratings: {}", responseJson);
            }

            return ResponseEntity.ok().header("Content-Type", "application/json").body(responseJson.toString());
        } else {
            logger.warn("\nToken '{}' is already invalid or doesnt exist.", article.getToken());

            return ResponseEntity.ok().build();
        }
    }

    @PostMapping(path = "/api", consumes = "application/json")
    public ResponseEntity<?> articleResponseEntity(@RequestBody Article article) {
        if (tokenService.isTokenValid(article.getToken())) {
            Article existingArticle = articleService.checkIfArticleAlreadyExists(article);

            if (existingArticle == null) {
                articleService.addArticle(article);
                switch (article.getRating()) {
                    case "true":
                        ratingService.addRating(new Rating(1, 0, 0, 0, article));
                        break;
                    case "false":
                        ratingService.addRating(new Rating(0, 1, 0, 0, article));
                        break;
                    case "misleading":
                        ratingService.addRating(new Rating(0, 0, 1, 0, article));
                        break;
                    case "unverified":
                        ratingService.addRating(new Rating(0, 0, 0, 1, article));
                        break;
                }
                System.out.println("New article: \n" + article.toString());
                System.out.println("-------------");
            } else {
                System.out.println("Existing article: \n" + existingArticle.toString());
                System.out.println("-------------");
                System.out.println("Existing article ID: \n" + existingArticle.getId());
                System.out.println("-------------");
                System.out.println("Incoming article: \n" + article.toString());
                System.out.println("-------------");
                ratingService.incrementRating(existingArticle, article);
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
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

//    @PostMapping("/api")
//    public ResponseEntity<?> getTitleViaAJAX(@RequestBody Article article, Errors errors) {
//
//        System.out.println(article.getTitle());
//
//        AjaxResponseBody result = new AjaxResponseBody();
//
//        //If error, just return a 400 bad request, along with the error message
//        if (errors.hasErrors()) {
//
//            result.setMessage(errors.getAllErrors()
//                    .stream().map(x -> x.getDefaultMessage())
//                    .collect(Collectors.joining(",")));
//
//            return ResponseEntity.badRequest().body(result);
//
//        }
//
////        List<User> users = userService.findByUserNameOrEmail(search.getUsername());
////        if (users.isEmpty()) {
////            result.setMsg("no user found!");
////        } else {
////            result.setMsg("success");
////        }
////        result.setResult(users);
//
//        return ResponseEntity.ok(result);
//    }
}
