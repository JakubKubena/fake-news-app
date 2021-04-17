package sk.kubena.fakenews.article;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> getAllArticles() {
        return new ArrayList<>(articleRepository.findAll());
    }

    public Article getArticleById(int id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Article getArticleByUrl(String url) {
        return articleRepository.findByUrl(url);
    }

    public void addArticle(ArticleDTO articleDTO) {
        PolicyFactory policy = Sanitizers.FORMATTING;
        String content = policy.sanitize(articleDTO.getContent());

        Article article = new Article();
        article.setUrl(articleDTO.getUrl());
        article.setHostname(articleDTO.getHostname());
        article.setTitle(articleDTO.getTitle());
        article.setContent(content);

        articleRepository.save(article);
    }

    public void updateArticle(Article article) {
        articleRepository.save(article);
    }

    public void deleteArticle(int id) {
        articleRepository.deleteById(id);
    }

    public boolean urlExists(String url) {
        return articleRepository.findByUrl(url) != null;
    }
}
