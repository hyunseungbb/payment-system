package boys.payment.web;

import boys.payment.dto.PaymentCancelDto;
import boys.payment.dto.PaymentDetailDto;
import boys.payment.entity.Payment;
import boys.payment.dto.PaymentDto;
import boys.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "payment", description = "결제시스템 API")
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "save payment", description = "결제하기")
    @ResponseBody
    @PostMapping("/")
    public ResponseEntity<PaymentDto.Response> savePayment(
            @RequestBody PaymentDto.Request payment
    ) throws Exception {
        return paymentService.savePayment(payment);
    }

    @ResponseBody
    @PostMapping("cancel")
    public ResponseEntity<PaymentCancelDto.Response> savePaymentCancel(
            @RequestBody PaymentCancelDto.Request paymentCancel
    ) throws Exception {
       return paymentService.savePaymentCancel(paymentCancel);
    }

    @ResponseBody
    @GetMapping("info")
    public ResponseEntity<PaymentDetailDto> getPayment(
            @RequestParam String paymentId
    ) throws Exception {
        return paymentService.getPaymentDetail(paymentId);
    }
}
