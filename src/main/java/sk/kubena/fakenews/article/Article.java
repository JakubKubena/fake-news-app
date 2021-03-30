package sk.kubena.fakenews.article;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import sk.kubena.fakenews.rating.Rating;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "url")
    private String url;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "title")
    private String title;

    @Transient
    private String rating;

    @Lob
    @Column(name = "content")
    private String content;

    @Transient
    private String token;

    @OneToOne(mappedBy = "articleId")
    @JsonIgnoreProperties("articleId")
    private Rating ratingId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public Article() {
    }

    public Article(String url, String hostname, String title, String rating, String content, String token, Timestamp createdAt, Timestamp updatedAt) {
        this.url = url;
        this.hostname = hostname;
        this.title = title;
        this.rating = rating;
        this.content = content;
        this.token = token;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Rating getRatingId() {
        return ratingId;
    }

    public void setRatingId(Rating ratingId) {
        this.ratingId = ratingId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "url= " + url + '\n' +
                "hostname= " + hostname + '\n' +
                "title= " + title + '\n' +
                "rating= " + rating + '\n' +
                "token= " + token + '\n' +
//                "content= " + content + '\n' +
                "createdAt= " + createdAt + '\n' +
                "updatedAt= " + updatedAt + '\n';
    }
}
