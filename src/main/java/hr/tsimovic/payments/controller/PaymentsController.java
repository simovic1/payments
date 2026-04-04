package hr.tsimovic.payments.controller;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    public ResponseEntity<?> addPayment(@RequestBody PaymentsRequest paymentsRequest) {
        PaymentsResponse response = paymentsService.createPayment(paymentsRequest);

        URI uri = URI.create("/api/payments/" + response.id());

        return ResponseEntity.created(uri).build();
    }
}
