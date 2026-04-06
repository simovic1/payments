package hr.tsimovic.payments.service;

import hr.tsimovic.payments.entity.Idempotency;
import hr.tsimovic.payments.enums.IdempotencyStatus;
import hr.tsimovic.payments.exception.IdempotencyConflictException;
import hr.tsimovic.payments.exception.RequestStillProcessingException;
import hr.tsimovic.payments.repository.IdempotencyRepository;
import hr.tsimovic.payments.helper.PaymentsTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdempotencyServiceTest {

    @Mock
    private IdempotencyRepository repository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void createProcessingRecord_WhenNoException_ShouldReturnIdempotency() {
        String key = UUID.randomUUID().toString();

        when(repository.saveAndFlush(any()))
                .thenReturn(PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH));

        Idempotency idempotency = idempotencyService.createProcessingRecord(key, PaymentsTestData.HASH);

        verify(repository, times(1)).saveAndFlush(any());
        assertNotNull(idempotency);
        assertEquals(IdempotencyStatus.PROCESSING, idempotency.getStatus());
        assertEquals(PaymentsTestData.HASH, idempotency.getRequestHash());
        assertEquals(key, idempotency.getIdempotencyKey());
        assertNotNull(idempotency.getExpiresAt());
        assertNull(idempotency.getResponsePayload());
    }

    @Test
    void createProcessingRecord_WhenDataIntegrityViolation_ShouldReturnIdempotency() {
        String key = UUID.randomUUID().toString();
        Idempotency existingIdempotency = PaymentsTestData.createExistingIdempotency(key, PaymentsTestData.HASH, PaymentsTestData.PAYLOAD);

        when(repository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);
        when(repository.findById(key))
                .thenReturn(Optional.of(existingIdempotency));

        Idempotency idempotency = idempotencyService.createProcessingRecord(key, PaymentsTestData.HASH);

        verify(repository, times(1)).saveAndFlush(any());
        verify(repository, times(1)).findById(key);
        assertNotNull(idempotency);
        assertEquals(IdempotencyStatus.COMPLETED, idempotency.getStatus());
        assertEquals(PaymentsTestData.HASH, idempotency.getRequestHash());
        assertEquals(key, idempotency.getIdempotencyKey());
        assertNotNull(idempotency.getExpiresAt());
        assertNotNull(idempotency.getResponsePayload());
    }

    @Test
    void createProcessingRecord_WhenDataIntegrityViolationAndIdempotencyNotFound_ShouldThrowIllegalStateException() {
        String key = UUID.randomUUID().toString();

        when(repository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);
        when(repository.findById(key))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> idempotencyService.createProcessingRecord(key, PaymentsTestData.HASH));
    }

    @Test
    void createProcessingRecord_WhenDataIntegrityViolationAndIdempotencyHashNotEqual_ShouldThrowIdempotencyConflictException() {
        String key = UUID.randomUUID().toString();
        Idempotency existingIdempotency = PaymentsTestData.createExistingIdempotency(key, PaymentsTestData.HASH, PaymentsTestData.PAYLOAD);

        when(repository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);
        when(repository.findById(key))
                .thenReturn(Optional.of(existingIdempotency));

        assertThrows(IdempotencyConflictException.class, () ->
                idempotencyService.createProcessingRecord(key, PaymentsTestData.HASH_2));
    }

    @Test
    void createProcessingRecord_WhenDataIntegrityViolationAndIdempotencyStatusIsProcessing_ShouldThrowRequestStillProcessing() {
        String key = UUID.randomUUID().toString();
        Idempotency existingIdempotency = PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH);

        when(repository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);
        when(repository.findById(key))
                .thenReturn(Optional.of(existingIdempotency));

        assertThrows(RequestStillProcessingException.class, () ->
                idempotencyService.createProcessingRecord(key, PaymentsTestData.HASH));
    }

    @Test
    void createProcessingRecord_WhenDataIntegrityViolationAndIdempotencyStatusCompletedWithoutPayload_ShouldThrowIdempotencyConflictException() {
        String key = UUID.randomUUID().toString();
        Idempotency existingIdempotency = PaymentsTestData.createExistingIdempotency(key, PaymentsTestData.HASH, PaymentsTestData.PAYLOAD);
        existingIdempotency.setResponsePayload(null);

        when(repository.saveAndFlush(any()))
                .thenThrow(DataIntegrityViolationException.class);
        when(repository.findById(key))
                .thenReturn(Optional.of(existingIdempotency));

        assertThrows(RequestStillProcessingException.class, () ->
                idempotencyService.createProcessingRecord(key, PaymentsTestData.HASH));
    }

    @Test
    void completeRecord_WhenIdempotencyExists_ShouldUpdateRecord() {
        String key = UUID.randomUUID().toString();
        Idempotency idempotency = PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH);

        when(repository.findById(key)).thenReturn(Optional.of(idempotency));

        idempotencyService.completeRecord(key, PaymentsTestData.PAYLOAD);

        verify(repository, times(1)).findById(key);
    }

    @Test
    void completeRecord_WhenIdempotencyDoesntExist_ShouldThrowIllegalStateException() {
        String key = UUID.randomUUID().toString();

        when(repository.findById(key)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                idempotencyService.completeRecord(key, PaymentsTestData.PAYLOAD));
    }

}
