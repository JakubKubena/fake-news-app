package sk.kubena.fakenews.model;

public class AjaxResponseBody {
    String message;
    Article article;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
