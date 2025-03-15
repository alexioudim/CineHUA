package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long seat_id;

    private String seat_row;
    private int seat_column;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Auditorium auditorium;

    public Seat() {
    }

    public String getRow() {
        return seat_row;
    }

    public void setRow(String row) {
        this.seat_row = seat_row;
    }

    public int getColumn() {
        return seat_column;
    }

    public void setColumn(int column) {
        this.seat_column = seat_column;
    }

    public Auditorium getAuditorium() {
        return auditorium;
    }

    public void setAuditorium(Auditorium auditorium) {
        this.auditorium = auditorium;
    }
}
