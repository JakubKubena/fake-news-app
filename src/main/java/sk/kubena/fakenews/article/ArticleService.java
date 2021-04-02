package sk.kubena.fakenews.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.kubena.fakenews.helper.CSVHelper;

import java.io.ByteArrayInputStream;
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
        Article article = new Article();
        article.setUrl(articleDTO.getUrl());
        article.setHostname(articleDTO.getHostname());
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        articleRepository.save(article);
    }

    public void updateArticle(int id, Article article) {
        articleRepository.save(article);
    }

    public void deleteArticle(int id) {
        articleRepository.deleteById(id);
    }

    public ByteArrayInputStream load() {
//        List<Article> articles = articleRepository.findAll();

        return CSVHelper.articlesToCSV(articleRepository.findAll());
    }

    public boolean urlExists(String url) {
        return articleRepository.findByUrl(url) != null;
    }
}
