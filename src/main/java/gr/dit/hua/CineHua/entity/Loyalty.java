package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "loyalty")
public class Loyalty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loyalty_id;

    private String loyaltyType;
    private float discountPercentage;
}
