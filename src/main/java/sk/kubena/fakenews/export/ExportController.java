package sk.kubena.fakenews.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.rating.RatingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class ExportController {

    private final ArticleService articleService;
    private final RatingService ratingService;

    @Autowired
    public ExportController(ArticleService articleService, RatingService ratingService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
    }

    @GetMapping("/export")
    public String export() {

        return "views/export";
    }

    @GetMapping("/export/file")
    public void exportToExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//        String currentDateTime = dateFormatter.format(new Date());
        // get request params and all articles
        String dateFrom = request.getParameter("from");
        String dateTo = request.getParameter("to");
        String fileName = request.getParameter("filename");
        String fileType = request.getParameter("filetype");
//        List<Article> listArticles = articleService.getAllArticles();
        List<Article> listArticles = articleService.getAllBetween(dateFrom, dateTo);

        // set response header
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + "." + fileType;
        response.setHeader(headerKey, headerValue);

        // check if file type is excel
        if (fileType.equals("xlsx")) {
            response.setContentType("application/octet-stream");

            ExcelExport excelExport = new ExcelExport(listArticles, ratingService);
            excelExport.export(response);

        // check if file type is csv
        } else if (fileType.equals("csv")) {
            response.setContentType("text/csv");

            CSVExport csvExport = new CSVExport(listArticles, ratingService);
            csvExport.export(response);
        }
    }
}
