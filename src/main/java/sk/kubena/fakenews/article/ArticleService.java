package sk.kubena.fakenews.article;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public Article getArticle(int id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Article getArticle(String url) {
        return articleRepository.findByUrl(url);
    }

    public void addArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        article.setUrl(articleDTO.getUrl());
        article.setHostname(articleDTO.getHostname());
        article.setTitle(articleDTO.getTitle());
        article.setUserRating(articleDTO.getUserRating());
        article.setContent(articleDTO.getContent());
        article.setToken(articleDTO.getToken());

        articleRepository.save(article);
    }

    public void updateArticle(int id, Article article) {
        articleRepository.save(article);
    }

    public void deleteArticle(int id) {
        articleRepository.deleteById(id);
    }

    @Transactional
    public Article checkIfArticleAlreadyExists(ArticleDTO articleDTO) {
        return articleRepository.findByUrl(articleDTO.getUrl());
    }

    public ByteArrayInputStream load() {
        List<Article> articles = articleRepository.findAll();

        return CSVHelper.articlesToCSV(articles);
    }
}
