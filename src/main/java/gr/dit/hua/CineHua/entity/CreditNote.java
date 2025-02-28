package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long creditNoteId;

    private float balance;

    private Date issueDate;

    private Date expirationDate;

    private CreditNoteStatus status;

    @ManyToOne
    private User issuer;
}

enum CreditNoteStatus {
    ACTIVE, EXPIRED, USED
}