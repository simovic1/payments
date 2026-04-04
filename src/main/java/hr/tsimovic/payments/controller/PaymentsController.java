package hr.tsimovic.payments.controller;

import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentsController {
    private final PaymentsService paymentsService;

    @GetMapping
    public ResponseEntity<List<PaymentsResponse>> getPayments() {
        return ResponseEntity.ok(paymentsService.getPayments());
    }
}
