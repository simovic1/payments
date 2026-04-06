package hr.tsimovic.payments.service;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.dto.PaymentsResult;
import hr.tsimovic.payments.entity.Idempotency;
import hr.tsimovic.payments.entity.Payment;
import hr.tsimovic.payments.enums.PaymentStatus;
import hr.tsimovic.payments.mapper.PaymentsMapper;
import hr.tsimovic.payments.repository.PaymentsRepository;
import hr.tsimovic.payments.utils.JsonPayloadService;
import hr.tsimovic.payments.helper.PaymentsTestData;
import hr.tsimovic.payments.utils.RequestHashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentsServiceTest {
    @Mock
    private PaymentsRepository paymentsRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private PaymentsMapper paymentsMapper;

    @Mock
    private RequestHashService requestHashService;

    @Mock
    private JsonPayloadService jsonPayloadService;

    @InjectMocks
    private PaymentsService paymentsService;

    @Test
    void getPayments_ShouldReturnPayments() {
        when(paymentsRepository.findAll()).thenReturn(Collections.emptyList());

        List<PaymentsResponse> paymentsResponses = paymentsService.getPayments();

        assertNotNull(paymentsResponses);
        assertTrue(paymentsResponses.isEmpty());
    }

    @Test
    void createPayment_WhenValidRequest_ShouldReturnPaymentResult() {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);

        Idempotency processingIdempotency = PaymentsTestData.createProcessingIdempotency(idempotencyKey, PaymentsTestData.HASH);
        PaymentsResponse response = PaymentsTestData.createPaymentsResponse(request);
        Payment payment = PaymentsTestData.createPayment(request);

        when(requestHashService.hashRequest(request)).thenReturn(PaymentsTestData.HASH);
        when(idempotencyService.createProcessingRecord(idempotencyKey, PaymentsTestData.HASH)).thenReturn(processingIdempotency);
        when(paymentsRepository.save(any())).thenReturn(payment);
        when(paymentsMapper.toPaymentsResponse(any())).thenReturn(response);
        when(jsonPayloadService.serialize(any())).thenReturn(PaymentsTestData.PAYLOAD);
        doNothing().when(idempotencyService).completeRecord(idempotencyKey, PaymentsTestData.PAYLOAD);

        PaymentsResult result = paymentsService.createPayment(request, idempotencyKey);

        assertTrue(result.isNew());
        assertNotNull(result.paymentsResponse());
        assertNotNull(result.paymentsResponse().id());
        assertNotNull(result.paymentsResponse().invoiceId());
        assertNotNull(result.paymentsResponse().amountMinor());
        assertNotNull(result.paymentsResponse().status());
        assertEquals(PaymentStatus.SUCCESS, result.paymentsResponse().status());

        verify(requestHashService, times(1)).hashRequest(request);
        verify(idempotencyService, times(1)).createProcessingRecord(idempotencyKey, PaymentsTestData.HASH);
        verify(paymentsRepository, times(1)).save(any());
        verify(paymentsMapper, times(1)).toPaymentsResponse(any());
        verify(jsonPayloadService, times(1)).serialize(response);
        verify(idempotencyService, times(1)).completeRecord(idempotencyKey, PaymentsTestData.PAYLOAD);
    }

    @Test
    void createPayment_WhenValidRequestAndIdempotencyExists_ShouldReturnPaymentResult() {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);

        Idempotency existingIdempotency = PaymentsTestData.createExistingIdempotency(idempotencyKey, PaymentsTestData.HASH, PaymentsTestData.PAYLOAD);
        PaymentsResponse response = PaymentsTestData.createPaymentsResponse(request);

        when(requestHashService.hashRequest(request)).thenReturn(PaymentsTestData.HASH);
        when(idempotencyService.createProcessingRecord(idempotencyKey, PaymentsTestData.HASH)).thenReturn(existingIdempotency);
        when(jsonPayloadService.deserialize(existingIdempotency.getResponsePayload(), PaymentsResponse.class)).thenReturn(response);

        PaymentsResult result = paymentsService.createPayment(request, idempotencyKey);

        assertFalse(result.isNew());
        assertNotNull(result.paymentsResponse());
        assertNotNull(result.paymentsResponse().id());
        assertNotNull(result.paymentsResponse().invoiceId());
        assertNotNull(result.paymentsResponse().amountMinor());
        assertNotNull(result.paymentsResponse().status());
        assertEquals(PaymentStatus.SUCCESS, result.paymentsResponse().status());

        verify(requestHashService, times(1)).hashRequest(request);
        verify(idempotencyService, times(1)).createProcessingRecord(idempotencyKey, PaymentsTestData.HASH);
        verify(paymentsRepository, times(0)).save(any());
    }
}
