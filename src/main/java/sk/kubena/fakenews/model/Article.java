package sk.kubena.fakenews.model;

public class Article {
    private String url;
    private String title;
    private String content;
    private String rating;

    public Article() {
    }

    public Article(String url, String title, String content, String rating) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.rating = rating;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
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
