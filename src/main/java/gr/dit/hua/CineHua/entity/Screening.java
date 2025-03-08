package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

@Entity
@Table(name = "screenings")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private Date date;

    @NotBlank
    private String startTime;

    @NotBlank
    private String endTime;

    @NotBlank
    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Auditorium auditorium;



}
