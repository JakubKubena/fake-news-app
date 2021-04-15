package sk.kubena.fakenews.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping(path = "/articles/{id}")
    public String deleteArticle(Model model, @PathVariable int id) {
        articleService.deleteArticle(id);
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
    }
}
