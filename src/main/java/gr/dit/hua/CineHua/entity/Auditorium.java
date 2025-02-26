package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Auditorium {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long roomId;

    private int capacity;
    private String name;

    @OneToMany(mappedBy = "auditorium")
    private List<Seat> seats;
}
