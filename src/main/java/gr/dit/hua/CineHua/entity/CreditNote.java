package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Entity
public class CreditNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditNote_id;

    @Column(nullable = false, unique = true)
    private String code;

    @NotNull
    private BigDecimal balance;

    @NotNull
    private LocalDateTime issueDate;

    @NotNull
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private CreditNoteStatus status;

    @Transient
    private String qrCode;

    @ManyToOne(optional = false)
    private User issuer;

    public CreditNote() {
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

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public User getIssuer() {
        return issuer;
    }

    public void setIssuer(User issuer) {
        this.issuer = issuer;
    }

}


