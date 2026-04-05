package hr.tsimovic.payments.dto;

public record PaymentsResult (boolean isNew, PaymentsResponse paymentsResponse) {
}
