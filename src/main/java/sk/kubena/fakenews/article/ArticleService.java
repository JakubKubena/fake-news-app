package sk.kubena.fakenews.article;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    public List<Article> getAllBetween(String from, String to) {
        Timestamp timestampFrom = null;
        Timestamp timestampTo = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateFrom = dateFormat.parse(from);
            Date dateTo = dateFormat.parse(to);
            timestampFrom = new java.sql.Timestamp(dateFrom.getTime());
            timestampTo = new java.sql.Timestamp(dateTo.getTime());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(articleRepository.findAllByCreatedAtBetween(timestampFrom, timestampTo));
    }

    public Article getArticleById(int id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Article getArticleByUrl(String url) {
        return articleRepository.findByUrl(url);
    }

    public HashMap<String, Integer> getHostnameCount() {
        HashMap<String, Integer> hashMap = new HashMap<>();
        List<String> hostnames = articleRepository.findDistinctHostnames();

        for (String hostName : hostnames) {
            hashMap.put(hostName, articleRepository.countByHostname(hostName));
        }

        return hashMap;
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
