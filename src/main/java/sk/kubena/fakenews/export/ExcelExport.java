package sk.kubena.fakenews.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.rating.RatingService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ExcelExport {

    private final List<Article> listArticles;
    private final RatingService ratingService;
    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public ExcelExport(List<Article> listArticles, RatingService ratingService) {
        this.listArticles = listArticles;
        this.ratingService = ratingService;
        workbook = new XSSFWorkbook();
    }

    private void createCell(Row row, int columnCount, Object value) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Articles");
        Row row = sheet.createRow(0);

        createCell(row, 0, "id");
        createCell(row, 1, "hostname");
        createCell(row, 2, "url");
        createCell(row, 3, "title");
        createCell(row, 4, "content");
        createCell(row, 5, "true");
        createCell(row, 6, "false");
        createCell(row, 7, "misleading");
        createCell(row, 8, "unverified");
        createCell(row, 9, "createdAt");
    }

    private void writeDataLines() {
        int rowCount = 1;

        for (Article article : listArticles) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            // The maximum length of cell contents (text) is 32767 characters
            String content = article.getContent().replaceAll("[\\t\\n]", "");
            int maxLength = 32000;
            if (content.length() > maxLength) {
                content = content.substring(0, maxLength);
            }

            createCell(row, columnCount++, article.getId());
            createCell(row, columnCount++, article.getHostname());
            createCell(row, columnCount++, article.getUrl());
            createCell(row, columnCount++, article.getTitle());
            createCell(row, columnCount++, content);
            createCell(row, columnCount++, ratingService.getRatingCount(article, "true"));
            createCell(row, columnCount++, ratingService.getRatingCount(article, "false"));
            createCell(row, columnCount++, ratingService.getRatingCount(article, "misleading"));
            createCell(row, columnCount++, ratingService.getRatingCount(article, "unverified"));
            createCell(row, columnCount, String.valueOf(article.getCreatedAt()));
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
