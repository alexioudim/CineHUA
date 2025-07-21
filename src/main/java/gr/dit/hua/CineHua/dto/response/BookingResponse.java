package gr.dit.hua.CineHua.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {

    private LocalDateTime issueDate;

    private BigDecimal totalPrice;

    private String bookingCode;

    private String qrCode;

    public BookingResponse(LocalDateTime issueDate, BigDecimal totalPrice, String bookingCode, String qrCode) {
        this.issueDate = issueDate;
        this.totalPrice = totalPrice;
        this.bookingCode = bookingCode;
        this.qrCode = qrCode;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}