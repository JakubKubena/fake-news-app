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

    public void incrementRating(Article existingArticle, Article newArticle) {
        Rating rating = ratingRepository.findRatingByArticleId(existingArticle);
        int count;
        switch (newArticle.getRating()) {
            case "true" :
                count = rating.getRating1();
                rating.setRating1(++count);
                break;
            case "false" :
                count = rating.getRating2();
                rating.setRating2(++count);
                break;
            case "misleading" :
                count = rating.getRating3();
                rating.setRating3(++count);
                break;
            case "satire" :
                count = rating.getRating4();
                rating.setRating4(++count);
                break;
        }
        ratingRepository.save(rating);
    }

    public Rating getRatingsOfArticle(Article existingArticle) {
        return ratingRepository.findRatingByArticleId(existingArticle);
    }
}
