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
import hr.tsimovic.payments.utils.RequestHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final IdempotencyService idempotencyService;
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

        Idempotency idempotency = idempotencyService.createProcessingRecord(idempotencyKey, requestHash);

        boolean isNewRequest = idempotency.getResponsePayload() == null;
        if (!isNewRequest) {
            PaymentsResponse response = jsonPayloadService.deserialize(idempotency.getResponsePayload(), PaymentsResponse.class);
            return new PaymentsResult(false, response);
        }

        Payment payment = new Payment();
        payment.setAmountMinor(paymentRequest.amountMinor());
        payment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        payment.setInvoiceId(paymentRequest.invoiceId());
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment saved = paymentsRepository.save(payment);
        PaymentsResponse response = paymentsMapper.toPaymentsResponse(saved);

        String responsePayload = jsonPayloadService.serialize(response);

        idempotencyService.completeRecord(idempotencyKey, responsePayload);

        return new PaymentsResult(true, response);
    }

}
