package sk.kubena.fakenews.export;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.rating.RatingService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CSVExport {

    private final List<Article> listArticles;
    private final RatingService ratingService;
    CSVPrinter csvPrinter = null;

    public CSVExport(List<Article> listArticles, RatingService ratingService) {
        this.listArticles = listArticles;
        this.ratingService = ratingService;
    }

    private void writeHeaderLine(HttpServletResponse response) throws IOException {
        final String[] HEADERS = { "id", "hostname", "url", "title", "content", "true", "false", "misleading", "unverified", "createdAt"};
        final CSVFormat format = CSVFormat.TDF
                .withHeader(HEADERS)
                .withQuoteMode(QuoteMode.ALL)
                .withEscape('\\')
                .withRecordSeparator('\n');

        csvPrinter = new CSVPrinter(response.getWriter(), format);
    }

    private void writeDataLines() throws IOException {
        for (Article article : listArticles) {
            List<String> data = Arrays.asList(
                    String.valueOf(article.getId()),
                    article.getHostname(),
                    article.getUrl(),
                    article.getTitle(),
                    article.getContent().replaceAll("[\\t]", ""),
                    String.valueOf(ratingService.getArticleRatingCount(article, "true")),
                    String.valueOf(ratingService.getArticleRatingCount(article, "false")),
                    String.valueOf(ratingService.getArticleRatingCount(article, "misleading")),
                    String.valueOf(ratingService.getArticleRatingCount(article, "unverified")),
                    String.valueOf(article.getCreatedAt())
            );
            csvPrinter.printRecord(data);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        try {
            writeHeaderLine(response);
            writeDataLines();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(csvPrinter != null) {
                csvPrinter.close();
            }
        }
    }
}
