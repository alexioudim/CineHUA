package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Auditorium {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long auditorium_id;

    private int capacity;
    private int rows;
    private int columns;

    private String name;

    @OneToMany(mappedBy = "auditorium", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Seat> seats;

    public Auditorium() {
    }

    public Long getAuditorium_id() {
        return auditorium_id;
    }

    public void setAuditorium_id(Long auditorium_id) {
        this.auditorium_id = auditorium_id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}
