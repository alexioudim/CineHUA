package gr.dit.hua.CineHua.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class BookingDTO{
    private long id;
    private String bookingCode;
    private LocalDateTime issueDate;
    private BigDecimal totalPrice;
    private String paymentStatus;     // PaymentStatus name
    private String paymentIntentId;
    private List<TicketDetails> tickets;

    @Getter
    @Setter
    public static class TicketDetails {
        private long id;
        private BigDecimal price;
        private String status; // TicketStatus name
        private SeatInfo seat;
        private ScreeningInfo screening;
    }

    @Getter
    @Setter
    public static class SeatInfo {
        private long id;
        private char row;    // ή int, ανάλογα με το entity σου
        private Integer column;
    }

    @Getter
    @Setter
    public static class ScreeningInfo {
        private long id;
        private String movieTitle;
        private String auditoriumName;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate date;
    }
}