package hr.tsimovic.payments.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RequestHashService {
    private final ObjectMapper objectMapper;

    public String hashRequest(Object request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            return DigestUtils.sha256Hex(json);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to hash request", ex);
        }
    }
}
