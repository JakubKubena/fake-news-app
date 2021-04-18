package sk.kubena.fakenews.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.user.User;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    int countByArticleAndValue(Article article, String value);
    int countByValue(String value);
    Rating findByArticleAndUser(Article article, User user);
}
