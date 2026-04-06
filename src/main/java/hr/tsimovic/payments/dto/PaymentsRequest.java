package hr.tsimovic.payments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentsRequest (
        @NotNull Long invoiceId,
        @NotNull @PositiveOrZero Long amountMinor) {
}
