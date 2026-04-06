package hr.tsimovic.payments.service;

import hr.tsimovic.payments.dto.PaymentsResult;
import hr.tsimovic.payments.exception.IdempotencyConflictException;
import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.entity.Idempotency;
import hr.tsimovic.payments.entity.Payment;
import hr.tsimovic.payments.enums.IdempotencyStatus;
import hr.tsimovic.payments.enums.PaymentStatus;
import hr.tsimovic.payments.exception.RequestStillProcessingException;
import hr.tsimovic.payments.mapper.PaymentsMapper;
import hr.tsimovic.payments.repository.IdempotencyRepository;
import hr.tsimovic.payments.repository.PaymentsRepository;
import hr.tsimovic.payments.utils.JsonPayloadService;
import hr.tsimovic.payments.utils.RequestHashService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsService {
    private final PaymentsRepository paymentsRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final PaymentsMapper paymentsMapper;

    private final RequestHashService requestHashService;
    private final JsonPayloadService jsonPayloadService;

    public List<PaymentsResponse> getPayments() {
        List<Payment> allPayments = paymentsRepository.findAll();
        return allPayments.stream()
                .map(paymentsMapper::toPaymentsResponse)
                .toList();
    }

    @Transactional
    public PaymentsResult createPayment(PaymentsRequest paymentRequest, String idempotencyKey) {
        log.info("Creating payment for idempotencyKey={}", idempotencyKey);
        String requestHash = requestHashService.hashRequest(paymentRequest);

        Optional<Idempotency> existingIdempotency = idempotencyRepository.findById(idempotencyKey);

        if (existingIdempotency.isPresent()) {
            log.info("Payment already exists for idempotencyKey={}", idempotencyKey);
            Idempotency idempotency = existingIdempotency.get();

            if (!idempotency.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException("Same idempotency key used with different payload");
            }

            if (IdempotencyStatus.PROCESSING.equals(idempotency.getStatus())) {
                throw new RequestStillProcessingException("Request is still being processed");
            }

            PaymentsResponse response = jsonPayloadService.deserialize(idempotency.getResponsePayload(), PaymentsResponse.class);
            return new PaymentsResult(false, response);
        }

        Idempotency idempotency = new Idempotency();
        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setRequestHash(requestHash);
        idempotency.setStatus(IdempotencyStatus.PROCESSING);
        idempotency.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusHours(24)));

        idempotencyRepository.save(idempotency);

        Payment payment = new Payment();
        payment.setAmountMinor(paymentRequest.amountMinor());
        payment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        payment.setInvoiceId(paymentRequest.invoiceId());
        payment.setStatus(PaymentStatus.PROCESSING);

        Payment saved = paymentsRepository.save(payment);
        PaymentsResponse response = paymentsMapper.toPaymentsResponse(saved);

        String responsePayload = jsonPayloadService.serialize(response);
        idempotency.setResponsePayload(responsePayload);
        idempotency.setStatus(IdempotencyStatus.COMPLETED);

        idempotencyRepository.save(idempotency);

        return new PaymentsResult(true, response);
    }


}
