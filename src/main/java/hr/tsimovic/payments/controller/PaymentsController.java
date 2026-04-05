package hr.tsimovic.payments.controller;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<PaymentsResponse> addPayment(@RequestHeader ("Idempotency-Key")  String idempotencyKey,
            @RequestBody PaymentsRequest paymentsRequest) {
        PaymentsResponse response = paymentsService.createPayment(paymentsRequest, idempotencyKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
