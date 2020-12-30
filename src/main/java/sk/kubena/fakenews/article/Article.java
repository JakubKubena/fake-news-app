package sk.kubena.fakenews.article;

import javax.persistence.*;

@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "url")
    private String url;

    @Column(name = "title")
    private String title;

    @Column(name = "rating")
    private String rating;

    @Lob
    @Column(name = "content")
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
        return "url= " + url + '\n' +
                "title= " + title + '\n' +
                "rating= " + rating + '\n' +
                "content= " + content.substring(0,100) + '\n' + '\n';
    }
}
