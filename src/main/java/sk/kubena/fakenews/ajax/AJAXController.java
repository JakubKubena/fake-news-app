package sk.kubena.fakenews.ajax;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.Rating;
import sk.kubena.fakenews.rating.RatingService;

@Controller
public class AJAXController {

//    Logger logger = LoggerFactory.getLogger(LoggingController.class);

    private final ArticleService articleService;
    private final RatingService ratingService;

    @Autowired
    public AJAXController(ArticleService articleService, RatingService ratingService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
    }

    @PostMapping(path = "/request-ratings", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> ratingResponseEntity(@RequestBody Article article) {
        System.out.println("article: \n" + article);
        Article existingArticle = articleService.checkIfArticleAlreadyExists(article);
        System.out.println("existingArticle: \n" + existingArticle);
        JSONObject responseJson = new JSONObject();

        if (existingArticle == null) {
            responseJson.put("rating1", 0);
            responseJson.put("rating2", 0);
            responseJson.put("rating3", 0);
            responseJson.put("rating4", 0);
        } else {
            System.out.println(existingArticle.toString());
            Rating rating = ratingService.getRatingsOfArticle(existingArticle);
            responseJson.put("rating1", rating.getRating1());
            responseJson.put("rating2", rating.getRating2());
            responseJson.put("rating3", rating.getRating3());
            responseJson.put("rating4", rating.getRating4());
        }
        System.out.println("responseJson: " + responseJson);

        return ResponseEntity.ok().header("Content-Type", "application/json").body(responseJson.toString());
    }

    @PostMapping(path = "/api", consumes = "application/json")
    public ResponseEntity<?> articleResponseEntity(@RequestBody Article article) {
        Article existingArticle = articleService.checkIfArticleAlreadyExists(article);

        if (existingArticle == null) {
            articleService.addArticle(article);
            switch (article.getRating()) {
                case "true" :
                    ratingService.addRating(new Rating(1, 0, 0, 0, article));
                    break;
                case "false" :
                    ratingService.addRating(new Rating(0, 1, 0, 0, article));
                    break;
                case "misleading" :
                    ratingService.addRating(new Rating(0, 0, 1, 0, article));
                    break;
                case "satire" :
                    ratingService.addRating(new Rating(0, 0, 0, 1, article));
                    break;
            }
            System.out.println("New article: \n" + article.toString());
        } else {
            System.out.println("Existing article: \n" + existingArticle.toString());
            System.out.println("EA ID: \n" + existingArticle.getId());
            System.out.println("inc article: \n" + article.toString());
            ratingService.incrementRating(existingArticle, article);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
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
