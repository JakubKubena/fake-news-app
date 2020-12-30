package sk.kubena.fakenews.ajax;

import sk.kubena.fakenews.article.Article;

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
