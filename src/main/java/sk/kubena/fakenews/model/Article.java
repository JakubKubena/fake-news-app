package sk.kubena.fakenews.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Article {

    @Id
    private int id;
    private String url;
    private String title;
    private String rating;
    private String content;

    public Article() {
    }

    public Article(String url, String title, String rating, String content) {
        this.url = url;
        this.title = title;
        this.rating = rating;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Article{" + '\n' +
                "url= " + url + '\n' +
                "title= " + title + '\n' +
                "rating= " + rating + '\n' +
                "content= " + '\n' + content + '\n' +
                '}';
    }
}
