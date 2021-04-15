package sk.kubena.fakenews.ajax;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.AlwaysQuoteMode;
import sk.kubena.fakenews.article.Article;
import sk.kubena.fakenews.article.ArticleDTO;
import sk.kubena.fakenews.article.ArticleService;
import sk.kubena.fakenews.export.ExcelExport;
import sk.kubena.fakenews.rating.Rating;
import sk.kubena.fakenews.rating.RatingService;
import sk.kubena.fakenews.user.User;
import sk.kubena.fakenews.user.UserDTO;
import sk.kubena.fakenews.user.UserService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class AJAXController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AJAXController.class);

    private final ArticleService articleService;
    private final RatingService ratingService;
    private final UserService userService;

    @Autowired
    public AJAXController(ArticleService articleService, RatingService ratingService, UserService userService) {
        this.articleService = articleService;
        this.ratingService = ratingService;
        this.userService = userService;
    }

    @GetMapping(path = "/login")
    public String login() {
        return "views/login";
    }

    @GetMapping("/success")
    public void loginPageRedirect(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {

        if(request.isUserInRole("ROLE_ADMIN")) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/home"));
        } else if(request.isUserInRole("ROLE_USER")){
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/profile"));
        }
    }

    // home
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
    }

    @GetMapping("/profile")
    public String user() {

        return "views/user";
    }

    @GetMapping("/export")
    public String export() {

        return "views/export";
    }

    // TODO: 14/04/2021 if we want to create users inside the app
//    @PostMapping(path = "/user")
//    public ResponseEntity<String> addUser(@RequestBody UserDTO userDTO) {
//        if (userDTO == null) {
//            LOGGER.info("Request body is null!");
//            return ResponseEntity.badRequest().body("Null request!");
//        } else {
//            userService.addUser(userDTO);
//            LOGGER.info("User '{}' has been successfully created.", userDTO.getEmail());
//            return ResponseEntity.ok(userService.authenticateUser(userDTO));
//        }
//    }

    // TODO: 31/03/2021 handle extension uninstallation
//    @GetMapping(path = "/uninstalled/{tokenString}")
//    public ResponseEntity<?> uninstalledResponseEntity(@PathVariable String tokenString) {
//        if (tokenService.isTokenValid(tokenString)) {
//            tokenService.invalidateToken(tokenString);
//            LOGGER.info("\nExtension '{}' was uninstalled and its token is now invalid.", tokenString);
//        } else {
//            LOGGER.warn("\nToken '{}' is already invalid or doesnt exist.", tokenString);
//        }
//
//        return ResponseEntity.ok().build();
//    }

    // TODO: 31/03/2021  make this method less awful, fix csrf
    // intercepts incoming user authentication requests
    @PostMapping(path = "/authenticate")
    public ResponseEntity<String> authenticateUser(@RequestBody UserDTO userDTO) {
        if (userService.authenticateUser(userDTO) == null) {
            LOGGER.info("Attempted login: '{}' : '{}'", userDTO.getEmail(), userDTO.getPassword());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wrong email or password!");
        } else {
            LOGGER.info("User '{}' has successfully logged in to an extension.", userDTO.getEmail());
            return ResponseEntity.ok(userService.authenticateUser(userDTO));
        }
    }

    // TODO: 01/04/2021 connect article and user through ID instead of token
    // intercepts incoming rating requests
    @PostMapping(path = "/request-ratings", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> ratingResponseEntity(@RequestBody ArticleDTO articleDTO) {

        // check if the request body is null
        if(articleDTO == null) {
            LOGGER.warn("Request body is null!");
            return ResponseEntity.badRequest().build();

        // check if the request body parameters are null
        } else if(articleDTO.getUrl() == null || articleDTO.getTitle() == null || articleDTO.getToken() == null) {
            LOGGER.warn("Null parameter in request body: {}", articleDTO.toString());
            return ResponseEntity.badRequest().build();

        // check if the user token is valid
        } else if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("Requested ratings for article: {}", articleDTO.toString());
            Article existingArticle = articleService.getArticleByUrl(articleDTO.getUrl());
            JSONObject responseJson = new JSONObject();

            // check if the article does not exist
            if (!articleService.urlExists(articleDTO.getUrl())) {
                // return 0 ratings
                responseJson.put("rating1", 0);
                responseJson.put("rating2", 0);
                responseJson.put("rating3", 0);
                responseJson.put("rating4", 0);
                responseJson.put("userRating", "null");
                LOGGER.info("No record of article found - sending 0 ratings.");

            // if the article does exist, then check if the rating of that article by that user does also exist
            } else if(ratingService.ratingExists(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()))) {
                // sending the article ratings along with the article rating by that user
                responseJson.put("rating1", ratingService.getRatingCount(existingArticle, "true"));
                responseJson.put("rating2", ratingService.getRatingCount(existingArticle, "false"));
                responseJson.put("rating3", ratingService.getRatingCount(existingArticle, "misleading"));
                responseJson.put("rating4", ratingService.getRatingCount(existingArticle, "unverified"));
                responseJson.put("userRating", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).getValue());
                LOGGER.warn("Rating of {} by {} found - sending ratings: {}", articleDTO.toString(), userService.getUserByToken(articleDTO.getToken()).toString(), responseJson);

            // if the article rating by that user does not exist, add new rating
            } else {
                responseJson.put("rating1", ratingService.getRatingCount(existingArticle, "true"));
                responseJson.put("rating2", ratingService.getRatingCount(existingArticle, "false"));
                responseJson.put("rating3", ratingService.getRatingCount(existingArticle, "misleading"));
                responseJson.put("rating4", ratingService.getRatingCount(existingArticle, "unverified"));
                responseJson.put("userRating", "null");
                LOGGER.info("Record of article found - sending ratings: {}", responseJson);
            }
            return ResponseEntity.ok().header("Content-Type", "application/json").body(responseJson.toString());

        // if user token is not valid
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());
            return ResponseEntity.badRequest().build();
        }
    }

    // TODO: 01/04/2021 check if rating value is 1 of the 4
    // intercepts the incoming article requests
    @PostMapping(path = "/api", consumes = "application/json")
    public ResponseEntity<?> articleResponseEntity(@RequestBody ArticleDTO articleDTO) {
        // check if the request body is null
        if(articleDTO == null) {
            LOGGER.warn("Request body is null!");
            return ResponseEntity.badRequest().build();

        // check if the request body parameters are null
        } else if(articleDTO.getUrl() == null || articleDTO.getHostname() == null || articleDTO.getTitle() == null ||
                articleDTO.getUserRating() == null || articleDTO.getContent() == null || articleDTO.getToken() == null) {
            LOGGER.warn("Null parameter in request body: {}", articleDTO.toString());
            return ResponseEntity.badRequest().build();

        // check if the user token is valid
        } else if (userService.tokenExists(articleDTO.getToken())) {
            LOGGER.info("token {} is valid.", articleDTO.getToken());
            LOGGER.info("Incoming article: {}", articleDTO.toString());

            // check if the article does not exist
            if (!articleService.urlExists(articleDTO.getUrl())) {
                // add new article
                articleService.addArticle(articleDTO);
                LOGGER.info("New article: {}", articleDTO.toString());

                // add new rating
                ratingService.addRating(new Rating(articleDTO.getUserRating(), articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())));
                LOGGER.info("New rating: {}", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).toString());
                return ResponseEntity.ok().build();

            // if the article does exist, then check if the rating of that article by that user does also exist
            } else if(ratingService.ratingExists(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()))) {
                LOGGER.warn("Rating of {} by {} already exists.", articleDTO.toString(), userService.getUserByToken(articleDTO.getToken()).toString());
                Rating rating = ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken()));
                rating.setValue(articleDTO.getUserRating());
                ratingService.updateRating(rating);
                return ResponseEntity.ok().build();

            // if the article rating by that user does not exist, add new rating
            } else {
                ratingService.addRating(new Rating(articleDTO.getUserRating(), articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())));
                LOGGER.info("New rating: {}", ratingService.getRatingByArticleAndUser(articleService.getArticleByUrl(articleDTO.getUrl()), userService.getUserByToken(articleDTO.getToken())).toString());
                return ResponseEntity.ok().build();
            }

        // if user token is not valid
        } else {
            LOGGER.warn("Token '{}' is invalid.", articleDTO.getToken());
            return ResponseEntity.badRequest().build();
        }
    }

//    @GetMapping("/export/excel")
//    public ResponseEntity<Workbook> exportToExcel() throws IOException {
//        Workbook workbook = new XSSFWorkbook();
//
//        Sheet sheet = workbook.createSheet("Articles");
//        sheet.setColumnWidth(0, 6000);
//        sheet.setColumnWidth(1, 4000);
//
//        Row header = sheet.createRow(0);
//
//        CellStyle headerStyle = workbook.createCellStyle();
//        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
//        font.setFontName("Arial");
//        font.setFontHeightInPoints((short) 16);
//        font.setBold(true);
//        headerStyle.setFont(font);
//
//        Cell headerCell = header.createCell(0);
//        headerCell.setCellValue("Name");
//        headerCell.setCellStyle(headerStyle);
//
//        headerCell = header.createCell(1);
//        headerCell.setCellValue("Age");
//        headerCell.setCellStyle(headerStyle);
//
//        CellStyle style = workbook.createCellStyle();
//        style.setWrapText(true);
//
//        Row row = sheet.createRow(2);
//        Cell cell = row.createCell(0);
//        cell.setCellValue("John Smith");
//        cell.setCellStyle(style);
//
//        cell = row.createCell(1);
//        cell.setCellValue(20);
//        cell.setCellStyle(style);
//
//        File currDir = new File(".");
//        String path = currDir.getAbsolutePath();
//        String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";
//
//        FileOutputStream outputStream = new FileOutputStream(fileLocation);
//        workbook.write(outputStream);
//        workbook.close();
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "temp.xlsx")
//                .contentType(MediaType.parseMediaType("application/octet-stream"))
//                .body(workbook);
//    }

    @GetMapping("/export/file")
    public void exportToExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String fileName = request.getParameter("filename");
        String fileType = request.getParameter("filetype");

        if (fileType.equals("xlsx")) {
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=" + fileName + "." + fileType;
            response.setHeader(headerKey, headerValue);
            response.setContentType("application/octet-stream");

            List<Article> listArticles = articleService.getAllArticles();

            ExcelExport excelExport = new ExcelExport(listArticles);

            excelExport.export(response);
        } else if (fileType.equals("csv")) {
//            String headerKey = "Content-Disposition";
//            String headerValue = "attachment; filename=" + fileName + "." + fileType;
//            response.setHeader(headerKey, headerValue);
//            response.setContentType("text/csv");
//
//            List<Article> listArticles = articleService.getAllArticles();
//
//            CsvPreference prefs = new CsvPreference.Builder('"',';',"\n")
//                    .useQuoteMode(new AlwaysQuoteMode()).build();
//
//            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), prefs);
//            String[] csvHeader = {"id", "hostname", "url", "title", "content", "createdAt"};
//            String[] nameMapping = {"id", "hostname", "url", "title", "content", "createdAt"};
//
//            csvWriter.writeHeader(csvHeader);
//
//            for (Article article : listArticles) {
//                csvWriter.write(article, nameMapping);
//            }
//
//            csvWriter.close();

            List<Article> articles = articleService.getAllArticles();
            CSVPrinter csvPrinter = null;
            try {
                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=" + fileName + "." + fileType;
                response.setHeader(headerKey, headerValue);
                response.setContentType("text/csv");

                final String[] HEADERS = { "id", "hostname", "url", "title", "content", "true", "false", "misleading", "unverified", "createdAt"};
                final CSVFormat format = CSVFormat.TDF
                        .withHeader(HEADERS)
                        .withQuoteMode(QuoteMode.ALL)
                        .withEscape('\\')
                        .withRecordSeparator('\n');

                csvPrinter = new CSVPrinter(response.getWriter(), format);

                for (Article article : articles) {
                    List<String> data = Arrays.asList(
                            String.valueOf(article.getId()),
                            article.getHostname(),
                            article.getUrl(),
                            article.getTitle(),
                            article.getContent().replaceAll("[\\t]", ""),
                            String.valueOf(ratingService.getRatingCount(article, "true")),
                            String.valueOf(ratingService.getRatingCount(article, "false")),
                            String.valueOf(ratingService.getRatingCount(article, "misleading")),
                            String.valueOf(ratingService.getRatingCount(article, "unverified")),
                            String.valueOf(article.getCreatedAt())
                    );
                    csvPrinter.printRecord(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(csvPrinter != null)
                    csvPrinter.close();
            }
        }
    }

//    @GetMapping("/export/excel")
//    public void exportToExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//        String currentDateTime = dateFormatter.format(new Date());
//        String fileName = request.getParameter("filename");
//        String fileType = request.getParameter("filetype");
//
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=" + fileName + "." + fileType;
//        response.setHeader(headerKey, headerValue);
//        response.setContentType("application/octet-stream");
//
//        List<Article> listArticles = articleService.getAllArticles();
//
//        ExcelExport excelExport = new ExcelExport(listArticles);
//
//        excelExport.export(response);
//        }
//
//    @GetMapping("/export/csv")
//    public ResponseEntity<Resource> exportToCSV() {
//        String filename = "articles.csv";
//        InputStreamResource file = new InputStreamResource(articleService.load());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                .contentType(MediaType.parseMediaType("application/csv"))
//                .body(file);
//    }

//    @GetMapping("/export")
//    public ResponseEntity<Resource> getFile() {
//        String filename = "articles.csv";
//        InputStreamResource file = new InputStreamResource(articleService.load());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
//                .contentType(MediaType.parseMediaType("application/csv"))
//                .body(file);
//    }

    @GetMapping("/ratings")
    public String ratings(Model model) {
        model.addAttribute("ratings", ratingService.getAllRatings());

        return "views/ratings";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());

        return "views/users";
    }

    @PutMapping(path = "/users/enabled", consumes = "application/json")
    public ResponseEntity<String> changeAccountStatus(@RequestBody String request) {

        if (request == null || request.isEmpty()) {
            LOGGER.info("Invalid request: {}", request);
            return ResponseEntity.badRequest().body("Invalid request!");

        } else {
            JSONObject jsonObject = new JSONObject(request);
            int id = Integer.parseInt(jsonObject.get("id").toString());
            boolean value = Boolean.parseBoolean(jsonObject.get("value").toString());

            if (userService.getUser(id) == null) {
                LOGGER.info("User not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");

            } else if (value) {
                LOGGER.info("User {} enabled!", id);
                userService.changeEnabledValue(id, true);
                return ResponseEntity.ok().body("User enabled!");

            } else {
                LOGGER.info("User {} disabled!", id);
                userService.changeEnabledValue(id, false);
                return ResponseEntity.ok().body("User disabled!");
            }
        }
    }

    @PutMapping(path = "/users/role", consumes = "application/json")
    public ResponseEntity<String> changeAccountRole(@RequestBody String request) {

        // check if request is null
        if (request == null || request.isEmpty()) {
            LOGGER.info("Invalid request: {}", request);
            return ResponseEntity.badRequest().body("Invalid request!");

        // if request is not null
        } else {
            JSONObject jsonObject = new JSONObject(request);
            int id = Integer.parseInt(jsonObject.get("id").toString());
            String role = jsonObject.get("role").toString();
            LOGGER.info("{} {}", id, role); 

            // check if user does not exist
            if (userService.getUser(id) == null) {
                LOGGER.info("User not found!");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");

            // if role is USER, change it to ADMIN
            } else if (role.equals("ROLE_USER")) {
                userService.changeRole(id, "ROLE_ADMIN");
                LOGGER.info("User {} promoted to ADMIN!", id);
                return ResponseEntity.ok().body("User promoted!");

            // if role is ADMIN, change it to USER
            } else if (role.equals("ROLE_ADMIN")) {
                userService.changeRole(id, "ROLE_USER");
                LOGGER.info("User {} demoted to USER!", id);
                return ResponseEntity.ok().body("User demoted!");

            // invalid role
            } else {
                LOGGER.info("Invalid role: {}", role);
                return ResponseEntity.badRequest().body("Invalid role!");
            }
        }
    }

    @PostMapping(path = "/articles/{id}")
    public String deleteArticle(Model model, @PathVariable int id) {
        articleService.deleteArticle(id);
        model.addAttribute("articles", articleService.getAllArticles());

        return "views/home";
    }
}
