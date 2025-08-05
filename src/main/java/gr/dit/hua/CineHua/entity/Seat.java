package gr.dit.hua.CineHua.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private char seat_row;
    private int seat_column;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Auditorium auditorium;

    public Seat() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public char getRow() {
        return seat_row;
    }

    public void setRow(char seat_row) {
        this.seat_row = seat_row;
    }

    public int getColumn() {
        return seat_column;
    }

    public void setColumn(int seat_column) {
        this.seat_column = seat_column;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public void setAuditorium(Auditorium auditorium) {
        this.auditorium = auditorium;
    }
}
