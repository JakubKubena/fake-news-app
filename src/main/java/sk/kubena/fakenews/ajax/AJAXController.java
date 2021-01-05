package sk.kubena.fakenews.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleService;

@Controller
public class AJAXController {

    @Autowired
    private ArticleService articleService;

    @PostMapping("/api")
    public ResponseEntity<?> postController(@RequestBody Article article) {
        articleService.addArticle(article);
        System.out.println(article.toString());

        return new ResponseEntity<>("Hello World!", HttpStatus.OK);
    }

    @RequestMapping("/home")
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
