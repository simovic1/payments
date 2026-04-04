package hr.tsimovic.payments.dto;

import hr.tsimovic.payments.enums.PaymentStatus;

import java.sql.Timestamp;

public record PaymentsResponse(
        Long id,
        Integer invoiceId,
        Double amount,
        PaymentStatus status,
        Timestamp createdAt
) {
}
