package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditNote_id;

    @Column(nullable = false, unique = true)
    private String code;

    private BigDecimal balance;

    private LocalDateTime issueDate;

    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private CreditNoteStatus status;

    @ManyToOne(optional = false)
    private User issuer;

    public CreditNote(BigDecimal balance) {
        this.balance = balance;
    }

    public CreditNote() {
    }

    @PrePersist
    public void onCreate() {
        this.issueDate = LocalDateTime.now();
        this.expirationDate = issueDate.plusMonths(1);
        this.status = CreditNoteStatus.ACTIVE;
        this.code = generateCreditNoteCode();
    }


    public Long getCreditNote_id() {
        return creditNote_id;
    }

    public void setCreditNote_id(Long creditNote_id) {
        this.creditNote_id = creditNote_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public CreditNoteStatus getStatus() {
        return status;
    }

    public void setStatus(CreditNoteStatus status) {
        this.status = status;
    }

    public User getIssuer() {
        return issuer;
    }

    public void setIssuer(User issuer) {
        this.issuer = issuer;
    }

    private String generateCreditNoteCode() {

        final String CHAR_POOL = "0123456789";
        final int CODE_LENGTH = 12;

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            code.append(CHAR_POOL.charAt(index));
        }

        return code.toString();
    }

}


enum CreditNoteStatus {
    ACTIVE, EXPIRED, USED
}