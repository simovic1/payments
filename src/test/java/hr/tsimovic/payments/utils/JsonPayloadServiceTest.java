package hr.tsimovic.payments.utils;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
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
public class JsonPayloadServiceTest {
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonPayloadService jsonPayloadService;

    @Test
    void serialize_WhenPayloadIsValid_ShouldSerialize() {
        String key = UUID.randomUUID().toString();
        Idempotency input = PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH);

        when(objectMapper.writeValueAsString(input)).thenReturn(PaymentsTestData.PAYLOAD);

        String result = jsonPayloadService.serialize(input);

        assertEquals(PaymentsTestData.PAYLOAD, result);
    }

    @Test
    void serialize_WhenPayloadIsInvalid_ShouldThrowIllegalStateException() {
        String key = UUID.randomUUID().toString();
        Idempotency input = PaymentsTestData.createProcessingIdempotency(key, PaymentsTestData.HASH);

        when(objectMapper.writeValueAsString(input)).thenThrow(IllegalStateException.class);

        assertThrows(IllegalStateException.class, () -> jsonPayloadService.serialize(input));
    }

    @Test
    void deserialize_WhenPayloadIsValid_ShouldDeserialize() {
        PaymentsResponse paymentsResponse = PaymentsTestData
                .createPaymentsResponse(new PaymentsRequest(12L, 1000L));

        when(objectMapper.readValue(PaymentsTestData.PAYLOAD, PaymentsResponse.class))
                .thenReturn(paymentsResponse);

        PaymentsResponse result = jsonPayloadService.deserialize(PaymentsTestData.PAYLOAD, PaymentsResponse.class);

        assertNotNull(result);
        assertEquals(12L, result.invoiceId());
        assertEquals(1000L, result.amountMinor());
    }

    @Test
    void derialize_WhenPayloadIsInvalid_ShouldThrowIllegalStateException() {
        when(objectMapper.readValue(PaymentsTestData.PAYLOAD, PaymentsResponse.class))
                .thenThrow(IllegalStateException.class);

        assertThrows(IllegalStateException.class, () -> jsonPayloadService.deserialize(PaymentsTestData.PAYLOAD, PaymentsResponse.class));
    }


}
