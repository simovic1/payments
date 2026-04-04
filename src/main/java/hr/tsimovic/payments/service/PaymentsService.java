package hr.tsimovic.payments.service;

import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.entity.Payments;
import hr.tsimovic.payments.mapper.PaymentsMapper;
import hr.tsimovic.payments.repository.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentsService {
    private final PaymentsRepository paymentsRepository;
    private final PaymentsMapper paymentsMapper;

    public List<PaymentsResponse> getPayments(){
        List<Payments> allPayments = paymentsRepository.findAll();
        return allPayments.stream()
                .map(paymentsMapper::toPaymentsResponse)
                .toList();
    }


}
