package sk.kubena.fakenews.helper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sk.kubena.fakenews.ajax.AJAXController;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.RatingService;
import sk.kubena.fakenews.user.UserService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Component
public class CSVHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AJAXController.class);

    private static RatingService ratingService;

    @Autowired
    public CSVHelper(RatingService ratingService) {
        CSVHelper.ratingService = ratingService;
    }

    public static ByteArrayInputStream articlesToCSV(List<Article> articles) {
        final String[] HEADERS = { "id", "hostname", "url", "title", "true", "false", "misleading", "unverified", "createdAt"};
        final CSVFormat format = CSVFormat.DEFAULT.withHeader(HEADERS).withQuoteMode(QuoteMode.ALL);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
            for (Article article : articles) {
                List<String> data = Arrays.asList(
                        String.valueOf(article.getId()),
                        article.getHostname(),
                        article.getUrl(),
                        article.getTitle(),
                        String.valueOf(ratingService.getRatingCount(article, "true")),
                        String.valueOf(ratingService.getRatingCount(article, "false")),
                        String.valueOf(ratingService.getRatingCount(article, "misleading")),
                        String.valueOf(ratingService.getRatingCount(article, "unverified")),
                        String.valueOf(article.getCreatedAt())
                );
                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to import data to CSV file: " + e.getMessage());
        }
    }
}
