package hr.tsimovic.payments.service;

import hr.tsimovic.payments.entity.Idempotency;
import hr.tsimovic.payments.enums.IdempotencyStatus;
import hr.tsimovic.payments.exception.IdempotencyConflictException;
import hr.tsimovic.payments.exception.RequestStillProcessingException;
import hr.tsimovic.payments.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Idempotency createProcessingRecord(String key, String hash) {
        try {
            Idempotency idempotency = new Idempotency();
            idempotency.setIdempotencyKey(key);
            idempotency.setRequestHash(hash);
            idempotency.setStatus(IdempotencyStatus.PROCESSING);
            idempotency.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusHours(24)));

            repository.saveAndFlush(idempotency);
            return idempotency;
        } catch (DataIntegrityViolationException e) {
            return handleExistingIdempotency(key, hash);
        }
    }

    private Idempotency handleExistingIdempotency(String key, String hash) {
        Idempotency idempotency = repository.findById(key)
                .orElseThrow(() -> new IllegalStateException("Idempotency record not found after duplicate key"));

        if (!idempotency.getRequestHash().equals(hash)) {
            throw new IdempotencyConflictException("Same idempotency key used with different payload");
        }

        if (IdempotencyStatus.PROCESSING.equals(idempotency.getStatus())) {
            throw new RequestStillProcessingException("Request is still being processed");
        }

        if (IdempotencyStatus.COMPLETED.equals(idempotency.getStatus()) &&
                idempotency.getResponsePayload() == null) {
            throw new RequestStillProcessingException("Completed idempotency record without response payload.");
        }

        return idempotency;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void completeRecord(String key, String responsePayload) {
        Idempotency idempotency = repository.findById(key)
                .orElseThrow(() -> new IllegalStateException("Idempotency record not found after duplicate key"));

        idempotency.setResponsePayload(responsePayload);
        idempotency.setStatus(IdempotencyStatus.COMPLETED);
    }
}
