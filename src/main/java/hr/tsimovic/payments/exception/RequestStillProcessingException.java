package hr.tsimovic.payments.exception;

public class RequestStillProcessingException extends RuntimeException {

    public RequestStillProcessingException(String message) {
        super(message);
    }
}
