package hr.tsimovic.payments.service;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.entity.Payment;
import hr.tsimovic.payments.enums.PaymentStatus;
import hr.tsimovic.payments.mapper.PaymentsMapper;
import hr.tsimovic.payments.repository.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsService {
    private final PaymentsRepository paymentsRepository;
    private final PaymentsMapper paymentsMapper;

    public List<PaymentsResponse> getPayments(){
        List<Payment> allPayments = paymentsRepository.findAll();
        return allPayments.stream()
                .map(paymentsMapper::toPaymentsResponse)
                .toList();
    }

    public PaymentsResponse createPayment(PaymentsRequest paymentRequest) {
        Payment payment = new Payment();
        payment.setAmount(paymentRequest.amount());
        payment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        payment.setInvoiceId(paymentRequest.invoiceId());
        payment.setStatus(PaymentStatus.PROCESSING);

        Payment saved = paymentsRepository.save(payment);

        return paymentsMapper.toPaymentsResponse(saved);
    }


}
