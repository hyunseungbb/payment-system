package boys.payment.dto;

import boys.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentCancelDto {

    @Schema
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String paymentId;
        private String price;
        private String vat = null;
    }

    @Schema
    @Getter
    public static class Response {
        private final String paymentId;
        private final String data;

        public Response(Payment payment) {
            this.paymentId = payment.getPaymentId();
            this.data = payment.getData();
        }
    }
}
