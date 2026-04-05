package hr.tsimovic.payments.entity;

import hr.tsimovic.payments.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "idempotency")
public class Idempotency {

    @Id
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_payload")
    private String responsePayload;

    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    @Column(name = "expires_at")
    private Timestamp expiresAt;
}
