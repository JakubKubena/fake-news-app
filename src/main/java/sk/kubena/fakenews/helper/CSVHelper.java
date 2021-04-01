package sk.kubena.fakenews.helper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import sk.kubena.fakenews.article.Article;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class CSVHelper {

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
                        String.valueOf(article.getRating().getRating1()),
                        String.valueOf(article.getRating().getRating2()),
                        String.valueOf(article.getRating().getRating3()),
                        String.valueOf(article.getRating().getRating4()),
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
