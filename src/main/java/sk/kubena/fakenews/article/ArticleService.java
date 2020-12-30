package sk.kubena.fakenews.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

//    private final ArticleRepository articleRepository;
//
//    @Autowired
//    public ArticleService(ArticleRepository articleRepository) {
//        this.articleRepository = articleRepository;
//    }

    public List<Article> getAllArticles() {
        return new ArrayList<>(articleRepository.findAll());
    }

    public Article getArticle(int id) {
        return articleRepository.findById(id).get();  // .findOne(id); --> .findById(id).get();
    }

    public void addArticle(Article article) {
        articleRepository.save(article);
    }

    public void updateArticle(int id, Article article) {
        articleRepository.save(article);
    }

    public void deleteArticle(int id) {
        articleRepository.deleteById(id);  // .delete(id); --> .deleteById(id);
    }
}
