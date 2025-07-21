package gr.dit.hua.CineHua.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditNoteResponse {

    private String code;

    private BigDecimal balance;

    private LocalDateTime issueDate;

    private LocalDateTime expirationDate;

    public CreditNoteResponse(String code, BigDecimal balance, LocalDateTime issueDate, LocalDateTime expirationDate) {
        this.code = code;
        this.balance = balance;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
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
}
