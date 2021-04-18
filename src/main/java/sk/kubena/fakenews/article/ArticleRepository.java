package sk.kubena.fakenews.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    List<Article> findAllByCreatedAtBetween(Timestamp from, Timestamp to);
    Article findByUrl(String url);
    int countByHostname(String hostname);

    @Query("SELECT DISTINCT hostname FROM Article")
    List<String> findDistinctHostnames();
}
