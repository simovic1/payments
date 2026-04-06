package hr.tsimovic.payments.controller;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.exception.IdempotencyConflictException;
import hr.tsimovic.payments.exception.RequestStillProcessingException;
import hr.tsimovic.payments.service.PaymentsService;
import hr.tsimovic.payments.helper.PaymentsTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentsController.class)
public class PaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentsService paymentsService;

    @Test
    void getPayments_ShouldReturnOKStatus() throws Exception {
        when(paymentsService.getPayments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/payments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addPayment_WhenValidRequestAndIdempotencyDoesntExist_ShouldReturnCreatedStatus() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);
        String requestJson = objectMapper.writeValueAsString(request);

        when(paymentsService.createPayment(request, idempotencyKey))
                .thenReturn(PaymentsTestData.createPaymentsResultWithStatusCompleted(request, Boolean.TRUE));


        mockMvc.perform(post("/api/payments")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    void addPayment_WhenValidRequestAndIdempotencyExist_ShouldReturnOkStatus() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);
        String requestJson = objectMapper.writeValueAsString(request);

        when(paymentsService.createPayment(request, idempotencyKey))
                .thenReturn(PaymentsTestData.createPaymentsResultWithStatusCompleted(request, Boolean.FALSE));


        mockMvc.perform(post("/api/payments")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void addPayment_WhenIdempotencyKeyMissing_ShouldThrowBadRequest() throws Exception {
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addPayment_WhenInvoiceIdPropertyMissing_ShouldThrowBadRequest() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(null, 1000L);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addPayment_WhenAmountMinorPropertyMissing_ShouldThrowBadRequest() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, null);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addPayment_WhenIdempotencyConflictException_ShouldThrowConflict() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);
        String requestJson = objectMapper.writeValueAsString(request);

        when(paymentsService.createPayment(request, idempotencyKey))
                .thenThrow(IdempotencyConflictException.class);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(requestJson))
                .andExpect(status().isConflict());
    }

    @Test
    void addPayment_WhenIdempotencyRequestStillProcessing_ShouldReturnAccepted() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        PaymentsRequest request = new PaymentsRequest(12L, 1000L);
        String requestJson = objectMapper.writeValueAsString(request);

        when(paymentsService.createPayment(request, idempotencyKey))
                .thenThrow(RequestStillProcessingException.class);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", idempotencyKey)
                        .content(requestJson))
                .andExpect(status().isAccepted());
    }
}
