package boys.payment.dto;

import boys.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PaymentDto {

    @Schema
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private String cardNumber;
        private String duration;
        private String cvc;
        private String installment;
        private String price;
        private String vat = null;

//        public Request(String cardNumber, String duration, String cvc, String installment, String price) {
//            this.cardNumber = cardNumber,
//            this.duration = cvc,
//            this.
//        }
    }

    @Schema
    @Getter
    public static class Response {
        private String paymentId;
        private String data;

        public Response(Payment payment) {
            this.paymentId = payment.getPaymentId();
            this.data = payment.getData();
        }
    }

}
