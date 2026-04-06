package hr.tsimovic.payments.utils;

import hr.tsimovic.payments.entity.Idempotency;
import hr.tsimovic.payments.helper.PaymentsTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestHashServiceTest {
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RequestHashService requestHashService;

    @Test
    void hashRequest_WhenObjectIsValid_ShouldHash() {
        String key = UUID.randomUUID().toString();
        Idempotency input = PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH);

        when(objectMapper.writeValueAsString(input)).thenReturn(PaymentsTestData.PAYLOAD);

        String result = requestHashService.hashRequest(input);

        assertNotNull(result);
    }

    @Test
    void hashRequest_WhenObjectIsNotValid_ShouldThrowRuntimeException() {
        String key = UUID.randomUUID().toString();
        Idempotency input = PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH);

        when(objectMapper.writeValueAsString(input)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> requestHashService.hashRequest(input));
    }
}
