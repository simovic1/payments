package hr.tsimovic.payments.mapper;

import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentsMapper {
    PaymentsResponse toPaymentsResponse(Payment payment);
}
