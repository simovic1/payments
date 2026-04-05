package hr.tsimovic.payments.exception;

public class IdempotencyConflictException extends RuntimeException {
    private String message;

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
