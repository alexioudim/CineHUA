package gr.dit.hua.CineHua.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScreeningRequest {

    @NotNull
    private long movie_id;

    @NotNull
    private long auditorium_id;

    @NotNull
    private LocalDate date;

    @NotNull
    private String startTime;

    public ScreeningRequest() {
    }

    public long getAuditorium_id() {
        return auditorium_id;
    }

    public void setAuditorium_id(long auditorium_id) {
        this.auditorium_id = auditorium_id;
    }

    public long getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(long movie_id) {
        this.movie_id = movie_id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

}
