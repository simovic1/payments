package hr.tsimovic.payments.dto;

public record PaymentsRequest (
        Long invoiceId,
        Double amount) {
}
