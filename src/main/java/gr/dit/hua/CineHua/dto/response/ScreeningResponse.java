package gr.dit.hua.CineHua.dto.response;

import gr.dit.hua.CineHua.entity.Screening;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScreeningResponse {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long movieId;
    private String movieTitle;
    private Long auditoriumId;
    private String auditoriumName;

    public ScreeningResponse(Screening screening) {
        this.id = screening.getId();
        this.date = screening.getDate();
        this.startTime = screening.getStartTime();
        this.endTime = screening.getEndTime();
        this.movieId = screening.getMovie().getId();
        this.movieTitle = screening.getMovie().getTitle();
        this.auditoriumId = screening.getAuditorium().getId();
        this.auditoriumName = screening.getAuditorium().getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Long getAuditoriumId() {
        return auditoriumId;
    }

    public void setAuditoriumId(Long auditoriumId) {
        this.auditoriumId = auditoriumId;
    }

    public String getAuditoriumName() {
        return auditoriumName;
    }

    public void setAuditoriumName(String auditoriumName) {
        this.auditoriumName = auditoriumName;
    }
}
