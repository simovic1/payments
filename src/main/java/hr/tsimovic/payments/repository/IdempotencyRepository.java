package hr.tsimovic.payments.repository;

import hr.tsimovic.payments.entity.Idempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, String> {
}
