package sk.kubena.fakenews.article;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ArticleDTO {

    @NotNull
    @NotEmpty
    private String url;

    @NotNull
    @NotEmpty
    private String hostname;

    @NotNull
    @NotEmpty
    private String title;

    @NotNull
    @NotEmpty
    private String userRating;

    @NotNull
    @NotEmpty
    private String content;

    @NotNull
    @NotEmpty
    private String token;

    public ArticleDTO(@NotNull @NotEmpty String url, @NotNull @NotEmpty String hostname, @NotNull @NotEmpty String title, @NotNull @NotEmpty String userRating, @NotNull @NotEmpty String content, @NotNull @NotEmpty String token) {
        this.url = url;
        this.hostname = hostname;
        this.title = title;
        this.userRating = userRating;
        this.content = content;
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "ArticleDTO{" +
                "url='" + url + '\'' +
                ", hostname='" + hostname + '\'' +
                ", title='" + title + '\'' +
                ", userRating='" + userRating + '\'' +
//                ", content='" + content + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
