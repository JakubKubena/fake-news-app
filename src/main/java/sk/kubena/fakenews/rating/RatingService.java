package sk.kubena.fakenews.rating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.user.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public List<Rating> getAllRatings() {
        return new ArrayList<>(ratingRepository.findAll());
    }

    public Rating getRatingById(int id) {
        return ratingRepository.findById(id).orElse(null);
    }

    public Rating getRatingByArticleAndUser(Article article, User user) {
        return ratingRepository.findByArticleAndUser(article, user);
    }

    public void addRating(Rating rating) {
        ratingRepository.save(rating);
    }

    public void updateRating(int id, Rating rating) {
        ratingRepository.save(rating);
    }

    public void deleteRating(int id) {
        ratingRepository.deleteById(id);
    }

    public int getRatingCount(Article article, String value) {
        return ratingRepository.countByArticleAndValue(article, value);
    }

    public boolean ratingExists(Article article, User user) {
        return ratingRepository.findByArticleAndUser(article, user) != null;
    }
}
