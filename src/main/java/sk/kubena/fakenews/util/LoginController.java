package sk.kubena.fakenews.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.RatingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class LoginController {

    private final ArticleService articleService;
    private final RatingService ratingService;

    @Autowired
    public LoginController(ArticleService articleService, RatingService ratingService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
    }

    @GetMapping("/")
    public String landingPage() {

        return "views/landingPage";
    }

    @GetMapping(path = "/login")
    public String login() {
        return "views/login";
    }

    @GetMapping("/success")
    public void loginPageRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(request.isUserInRole("ROLE_ADMIN")) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/home"));
        } else if(request.isUserInRole("ROLE_USER")){
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/profile"));
        }
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());
        model.addAttribute("hostnameCounts", articleService.getHostnameCount());
        model.addAttribute("ratingCounts", ratingService.getRatingCount());

        return "views/home";
    }

    @GetMapping("/profile")
    public String profile() {

        return "views/profile";
    }
}
