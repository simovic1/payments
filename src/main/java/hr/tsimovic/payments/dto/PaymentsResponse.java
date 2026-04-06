package hr.tsimovic.payments.dto;

import hr.tsimovic.payments.enums.PaymentStatus;

import java.sql.Timestamp;

public record PaymentsResponse(
        Long id,
        Long invoiceId,
        Long amountMinor,
        PaymentStatus status,
        Timestamp createdAt
) {
}
