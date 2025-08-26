package gr.dit.hua.CineHua.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingValidationResponse {

    public enum Status {
        VALID_COLLECTED,          // επιτυχής έλεγχος + συλλογή
        INVALID_NOT_FOUND,        // δεν βρέθηκε booking
        INVALID_PAYMENT,          // πληρωμή όχι επιτυχής
        INVALID_ALREADY_COLLECTED,// όλα τα tickets ήδη collected
        INVALID_PARTIALLY_USED,   // κάποια tickets είναι ήδη collected/cancelled
        INVALID_TIME_WINDOW       // εκτός επιτρεπτού χρονικού παραθύρου
    }

    private boolean valid;
    private Status status;
    private String bookingCode;
    private Long bookingId;
    private int totalTickets;
    private int collectedNow;       // πόσα έγιναν collect τώρα
    private String message;

    private Long screeningId;
    private String movieTitle;
    private String auditoriumName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;


    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getCollectedNow() {
        return collectedNow;
    }

    public void setCollectedNow(int collectedNow) {
        this.collectedNow = collectedNow;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getAuditoriumName() {
        return auditoriumName;
    }

    public void setAuditoriumName(String auditoriumName) {
        this.auditoriumName = auditoriumName;
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
}