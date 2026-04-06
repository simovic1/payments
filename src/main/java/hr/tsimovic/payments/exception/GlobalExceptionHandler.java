package hr.tsimovic.payments.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflictException(IdempotencyConflictException e) {
        ErrorResponse errorResponse = new ErrorResponse("IDEMPOTENT_CONFLICT", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(RequestStillProcessingException.class)
    public ResponseEntity<ErrorResponse> handleRequestStillProcessingException(RequestStillProcessingException e) {
        ErrorResponse errorResponse = new ErrorResponse("REQUEST_STILL_PROCESSING", e.getMessage());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);
    }
}
