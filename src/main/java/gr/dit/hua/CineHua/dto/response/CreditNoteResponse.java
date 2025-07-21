package gr.dit.hua.CineHua.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreditNoteResponse {

    private String code;

    private BigDecimal balance;

    private LocalDateTime issueDate;

    private LocalDateTime expirationDate;

    private String qrCode;

    public CreditNoteResponse(String code, BigDecimal balance, LocalDateTime issueDate, LocalDateTime expirationDate, String qrCode) {
        this.code = code;
        this.balance = balance;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
