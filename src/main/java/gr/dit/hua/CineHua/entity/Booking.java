package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long booking_id;

    private Date issueDate;

    private float totalPrice;

    @ManyToOne
    private User issuer;

    @OneToMany(mappedBy = "booking")
    private List<Ticket> tickets;
}
