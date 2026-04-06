package hr.tsimovic.payments.helper;

import hr.tsimovic.payments.dto.PaymentsRequest;
import hr.tsimovic.payments.dto.PaymentsResponse;
import hr.tsimovic.payments.dto.PaymentsResult;
import hr.tsimovic.payments.entity.Idempotency;
import hr.tsimovic.payments.entity.Payment;
import hr.tsimovic.payments.enums.IdempotencyStatus;
import hr.tsimovic.payments.enums.PaymentStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PaymentsTestData {

    public static final String PAYLOAD = """
            {"id":1,"invoiceId":12,"amountMinor":1000,"paymentStatus":"SUCCESS","createdAt":"2026-04-06T14:32:15.123"}
            """;

    public static final String HASH = """
            a0121d41f26084c5b2df3f5cf609576367fe43415a75979c98734afdb06c8e14
            72f9c1e3896997f94b88f626a2b948f5f2b20baadc9e11237a327705db9c81d3
            8aa3f2757a0efb737184ded4b99d3588b3d494468054144ec9ef9ff407a7a63a
            c66a151f5e7368d99a756b17d93d88671ac0a682a5e2d5e5fa36a5014543c45f
            a9373d8854f0856e8a7470cd8d0fceb93cd5e4922249c46ff9978903b00f7f74
            bc9cfb139e5bc6be5eeefe1f2f80707a4fd3b1c9793ad8c9f3e2c1628681b0ca
            9230d5f245bff0877f52222826b842c682bc82f4f494f3a428562bed651f0b13
            1d36045ed72a8339c830e7904d64cd0ae0b2c97a31257ca5c16d39bc952b4f4a
            31ac88ffae1915aa8e0bc040756e7e01d3aaa9d1b7a7b675faf247374d487978
            e47ef8c469372e2c740c7401f46f1973cf3b9f564bb771a2a427214ab9b707ee
            """;

    public static final String HASH_2 = """
            c5c30e94805edddc9b3d73cc5c006adf3e2f39f80583701ecfe5c17d83ee3ba8
            172350b9822bd1b71fc3aa38f40c64fd88d86aabbb7e3a48c77270a7f30dc664
            3af7968daa4e38c5af82850246f18806e9a99a68598908445e90b8b847e5dfe1
            edcbea80c578ea16ed604013c3372c9cd6a082c93e84fb1eebf188ed358c0d2b
            4c7df8088df821f830e1e50a6e99f75d7aa62043acd71564959541a159fa82c1
            58d7b385cec710f5273f802844952b40dbbed28a6af07f46e1c81689701ccfda
            62111c99995ddd891a8caae12bf8be7ad6913bc53d6df6027553b1b858deb710
            910735406e016e37d42f04032f9f36b58dbc7c84a4334afb281faadea2ebfd27
            ccfb4f44db501369aedcb40815ff163a0891814c40d0133659a7151b1e19fd50
            e463edd71fe990c8ed957bc2f4b3c8ba19fa65d1aa4386a2309e619c63989b17
            """;

    public static PaymentsResult createPaymentsResultWithStatusCompleted(PaymentsRequest request, Boolean isNewRecord) {
        PaymentsResponse response = createPaymentsResponse(request);
        return new PaymentsResult(isNewRecord, response);
    }

    public static PaymentsResponse createPaymentsResponse(PaymentsRequest request) {
        return new PaymentsResponse(1L, request.invoiceId(), request.amountMinor(), PaymentStatus.SUCCESS, Timestamp.valueOf(LocalDateTime.now()));
    }

    public static Idempotency createProcessingIdempotency(String key, String hash) {
        Idempotency idempotency = createBasicIdempotency(key, hash);
        idempotency.setStatus(IdempotencyStatus.PROCESSING);

        return idempotency;
    }

    public static Idempotency createExistingIdempotency(String key, String hash, String payload) {
        Idempotency idempotency = createBasicIdempotency(key, hash);
        idempotency.setResponsePayload(payload);
        idempotency.setStatus(IdempotencyStatus.COMPLETED);

        return idempotency;
    }

    public static Payment createPayment(PaymentsRequest request) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setInvoiceId(request.invoiceId());
        payment.setAmountMinor(request.amountMinor());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        return payment;
    }

    private static Idempotency createBasicIdempotency(String key, String hash) {
        Idempotency idempotency = new Idempotency();
        idempotency.setIdempotencyKey(key);
        idempotency.setRequestHash(hash);
        idempotency.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusHours(24)));

        return idempotency;
    }
}
