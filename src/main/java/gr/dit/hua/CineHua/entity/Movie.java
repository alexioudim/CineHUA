package gr.dit.hua.CineHua.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String title;

    @NotBlank
    private String duration;

    @NotBlank
    private String rating;

    @NotBlank
    private String synopsis;

    @NotBlank
    private String genre;

    @NotBlank
    private Date releaseDate;
}
