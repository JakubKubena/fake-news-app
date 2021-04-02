package sk.kubena.fakenews.rating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.kubena.fakenews.article.Article;

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

    public Rating getRating(int id) {
        return ratingRepository.findById(id).orElse(null);
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
}
