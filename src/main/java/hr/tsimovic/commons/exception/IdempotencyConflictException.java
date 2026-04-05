package hr.tsimovic.commons.exception;

public class IdempotencyConflictException extends RuntimeException {
    private String message;

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
