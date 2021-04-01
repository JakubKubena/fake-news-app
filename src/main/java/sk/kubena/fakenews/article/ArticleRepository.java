package sk.kubena.fakenews.article;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    Article findByUrl(String url);
}
