package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditNote_id;



    private BigDecimal balance;

    private LocalDateTime issueDate;

    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private CreditNoteStatus status;

    @ManyToOne(optional = false)
    private User issuer;

    @PrePersist
    public void onCreate() {
        this.issueDate = LocalDateTime.now();
        this.expirationDate = issueDate.plusMonths(1);
        this.status = CreditNoteStatus.ACTIVE;
    }
}

enum CreditNoteStatus {
    ACTIVE, EXPIRED, USED
}