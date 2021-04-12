package sk.kubena.fakenews.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.rating.RatingService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ExcelExport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Article> listArticles;
//    private RatingService ratingService;

    @Autowired
    public ExcelExport(List<Article> listArticles/*, RatingService ratingService*/) {
        this.listArticles = listArticles;
//        this.ratingService = ratingService;
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

    //    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
//        sheet.autoSizeColumn(columnCount);
//        Cell cell = row.createCell(columnCount);
//        if (value instanceof Integer) {
//            cell.setCellValue((Integer) value);
//        } else if (value instanceof Boolean) {
//            cell.setCellValue((Boolean) value);
//        }else {
//            cell.setCellValue((String) value);
//        }
//        cell.setCellStyle(style);
//    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Articles");

        Row row = sheet.createRow(0);

//        CellStyle style = workbook.createCellStyle();
//        XSSFFont font = workbook.createFont();
//        font.setBold(true);
//        font.setFontHeight(16);
//        style.setFont(font);
//
//        createCell(row, 0, "id", style);
//        createCell(row, 1, "hostname", style);
//        createCell(row, 2, "url", style);
//        createCell(row, 2, "title", style);
//        createCell(row, 2, "content", style);
//        createCell(row, 3, "createdAt", style);

        createCell(row, 0, "id");
        createCell(row, 1, "hostname");
        createCell(row, 2, "url");
        createCell(row, 3, "title");
//        createCell(row, 4, "content");
        createCell(row, 4, "createdAt");
    }

    private void writeDataLines() {
        int rowCount = 1;

//        CellStyle style = workbook.createCellStyle();
//        XSSFFont font = workbook.createFont();
//        font.setFontHeight(14);
//        style.setFont(font);
//
//        for (Article article : listArticles) {
//            Row row = sheet.createRow(rowCount++);
//            int columnCount = 0;
//
//            createCell(row, columnCount++, article.getId(), style);
//            createCell(row, columnCount++, article.getHostname(), style);
//            createCell(row, columnCount++, article.getUrl(), style);
//            createCell(row, columnCount++, article.getTitle(), style);
//            createCell(row, columnCount++, article.getContent(), style);
////            createCell(row, columnCount++, ratingService.getRatingCount(article, "true"), style);
////            createCell(row, columnCount++, ratingService.getRatingCount(article, "false"), style);
////            createCell(row, columnCount++, ratingService.getRatingCount(article, "misleading"), style);
////            createCell(row, columnCount++, ratingService.getRatingCount(article, "unverified"), style);
//            createCell(row, columnCount++, String.valueOf(article.getCreatedAt()), style);
//        }

        for (Article article : listArticles) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, article.getId());
            createCell(row, columnCount++, article.getHostname());
            createCell(row, columnCount++, article.getUrl());
            createCell(row, columnCount++, article.getTitle());
//            createCell(row, columnCount++, article.getContent());
//            createCell(row, columnCount++, ratingService.getRatingCount(article, "true"), style);
//            createCell(row, columnCount++, ratingService.getRatingCount(article, "false"), style);
//            createCell(row, columnCount++, ratingService.getRatingCount(article, "misleading"), style);
//            createCell(row, columnCount++, ratingService.getRatingCount(article, "unverified"), style);
            createCell(row, columnCount++, String.valueOf(article.getCreatedAt()));
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
