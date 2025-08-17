package gr.dit.hua.CineHua.dto.response;

import java.math.BigDecimal;

public record RefundResponse(String refundId, String status, BigDecimal refundedAmount) {}