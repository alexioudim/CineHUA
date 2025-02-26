package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

@Entity
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long seatId;

    private String row;
    private int column;

    @ManyToOne
    private Auditorium auditorium;
}
