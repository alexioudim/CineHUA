package gr.dit.hua.CineHua.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

@Entity
@Table(name = "movies")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Runtime")
    private String duration;

    @JsonProperty("Rated")
    private String rating;

    @JsonProperty("Plot")
    private String synopsis;

    @JsonProperty("Genre")
    private String genre;

    @JsonProperty("Released")
    private String releaseDate;

    public Movie(String title, String duration, String rating, String synopsis, String genre, String releaseDate) {
        this.title = title;
        this.duration = duration;
        this.rating = rating;
        this.synopsis = synopsis;
        this.genre = genre;
        this.releaseDate = releaseDate;
    }

    public Movie() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
