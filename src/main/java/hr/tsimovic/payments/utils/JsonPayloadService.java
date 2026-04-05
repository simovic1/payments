package hr.tsimovic.payments.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JsonPayloadService {
    private final ObjectMapper objectMapper;

    public String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize object", e);
        }
    }

    public <T> T deserialize(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to deserialize object", e);
        }
    }
}
