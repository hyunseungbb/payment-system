package boys.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailDto {

    private String paymentId;
    private String cardNumber;
    private String duration;
    private String cvc;
    private String typ;
    private String price;
    private String vat;

}
