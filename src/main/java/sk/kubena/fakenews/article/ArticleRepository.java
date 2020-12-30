package sk.kubena.fakenews.article;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.kubena.fakenews.article.Article;

public interface ArticleRepository extends JpaRepository<Article, Integer> {
}
