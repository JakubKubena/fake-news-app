package sk.kubena.fakenews.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.kubena.fakenews.model.Article;

public interface ArticleRepository extends JpaRepository<Article, Integer> {
}
