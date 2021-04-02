package sk.kubena.fakenews.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.kubena.fakenews.article.Article;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    int countByArticleAndValue(Article article, String value);
}
