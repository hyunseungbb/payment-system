package boys.payment.service;

import boys.payment.dto.PaymentCancelDto;
import boys.payment.dto.PaymentDetailDto;
import boys.payment.dto.PaymentDto;
import boys.payment.entity.Balance;
import boys.payment.entity.Payment;
import boys.payment.entity.PaymentSequence;
import boys.payment.repository.BalanceRepository;
import boys.payment.repository.PaymentRepository;
import boys.payment.repository.PaymentSequenceRepository;
import boys.payment.util.AES256;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BalanceRepository balanceRepository;
    private final PaymentSequenceRepository paymentSequenceRepository;

    @Transactional
    public ResponseEntity<PaymentDto.Response> savePayment(PaymentDto.Request paymentReq) throws Exception {
        // id 생성
        Long id = paymentSequenceRepository.save(new PaymentSequence()).getId();
        String paymentId = getPaymentId(id);

        // 부가가치세 계산
        if (paymentReq.getVat() == null){
            int vat = Math.round((float)Integer.parseInt(paymentReq.getPrice()) / 11);
            paymentReq.setVat(Integer.toString(vat));
        }
        // 암호화
        Payment encryptedPayment = encryptPayment(paymentReq, paymentId);
        // db 저장
        PaymentDto.Response paymentRes = new PaymentDto.Response(paymentRepository.save(encryptedPayment));
        Balance balance = new Balance(paymentId, Integer.parseInt(paymentReq.getPrice()));
        balanceRepository.save(balance);

        return new ResponseEntity<>(paymentRes, HttpStatus.OK);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<PaymentCancelDto.Response> savePaymentCancel(PaymentCancelDto.Request paymentCancelReq) throws Exception {
        // 결제 정보 가져오기
        String paymentId = paymentCancelReq.getPaymentId();
//        Balance balance = balanceRepository.findById(paymentId).orElseThrow();
        Payment encryptedPayment = paymentRepository.findById(paymentId).orElseThrow();
//        Payment encryptedPayment = paymentRepository.findByPaymentId(paymentId).orElseThrow();
//        Payment encryptedPayment = paymentRepository.findByIdForUpdate(paymentId);
        PaymentDetailDto paymentDetailDto = decryptPayment(encryptedPayment);

        // 일단 전체취소인지 부분취소인지 파악해야 함.
        /*
         - 취소요청 타당성 검증
         - 오류나는 경우
         1. 누적취소금액 > 결제금액
         2. 누적부가가치세 > 결제부가가치세
         누적취소금액 = 결제금액 -> 누적부가가치세 = 결제부가가치세
         누적부가가치세 = 결제부가가치세 -> 누적취소금액 = 결제금액
        * */
        int paymentVat = Integer.parseInt(paymentDetailDto.getVat());
        int paymentPrice = Integer.parseInt(paymentDetailDto.getPrice());
        Iterable<Payment> paymentCancelList = paymentRepository.findAllByPaymentIdInData(paymentId);
        for (Payment paymentCancel : paymentCancelList) {
            System.out.println("취소 : " + paymentCancel.getPaymentId());
        }
        int totalCancelVat = 0;
        int totalCancelPrice = Integer.parseInt(paymentCancelReq.getPrice());
        for (Payment paymentCancel : paymentCancelList) {
            // *******인덱스 찾기
            totalCancelVat += Integer.parseInt(paymentCancel.getData().substring(73, 83).replaceFirst("^0+(?!$)", ""));
            totalCancelPrice += Integer.parseInt(paymentCancel.getData().substring(63, 73).strip());
            System.out.println(paymentCancel.getPaymentId() + " : " + paymentCancel.getData().substring(63, 73).strip() + "원");
        }

        // 부가가치세 세팅
        if (paymentCancelReq.getVat() == null) {
            int vat;
            // 초기결재금액 = 전체취소금액
            if (paymentPrice == totalCancelPrice) {
                vat = paymentVat - totalCancelVat;
            } else {
            // 그외의 경우 부가가치세 기본계산법으로 산정
                vat = Math.round((float)Integer.parseInt(paymentCancelReq.getPrice()) / 11);
            }
            paymentCancelReq.setVat(Integer.toString(vat));
        }
        totalCancelVat += Integer.parseInt(paymentCancelReq.getVat());

        // 취소 Validation
        if (totalCancelPrice > paymentPrice || totalCancelVat > paymentVat) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (totalCancelPrice == paymentPrice) {
            if (totalCancelVat != paymentVat) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (totalCancelVat == paymentVat) {
            if (totalCancelPrice != paymentPrice) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        System.out.println("");
        System.out.println("총 취소 가격 : " + totalCancelPrice + "원");
        // id 생성
        Long id = paymentSequenceRepository.save(new PaymentSequence()).getId();
        String paymentCancelId = getPaymentId(id);
        // 암호화
        Payment encryptedPaymentCancel = encryptPaymentCancel(paymentDetailDto, paymentCancelId, paymentCancelReq);
        // db 저장
        PaymentCancelDto.Response paymentCancelRes = new PaymentCancelDto.Response(paymentRepository.save(encryptedPaymentCancel));
        Balance balance = balanceRepository.findById(paymentId).orElse(new Balance(paymentId,0));
        balance.setPrice(paymentPrice - totalCancelPrice);
        balanceRepository.save(balance);
        return new ResponseEntity<>(paymentCancelRes, HttpStatus.OK);
    }

    public ResponseEntity<PaymentDetailDto> getPaymentDetail(String paymentId) throws Exception {
        // db 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        // 복호화
        PaymentDetailDto paymentDetail = decryptPayment(payment);
        return new ResponseEntity<>(paymentDetail, HttpStatus.OK);
    }

    private String getPaymentId(Long id){
        // YYYYMMDD(id)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new Date());
        return date + String.format("%012d", id);
    }

    private Payment encryptPayment(PaymentDto.Request payment, String id) throws Exception {
        String body = "";
        // 데이터 부문
        String cardNumber = String.format("%-20s", payment.getCardNumber());
        String duration = String.format("%-4s", payment.getDuration());
        String cvc = String.format("%-3s", payment.getCvc());
        body += cardNumber;
        body += String.format("%02d", Integer.parseInt(payment.getInstallment()));
        body += duration;
        body += cvc;
        body += String.format("%10s", payment.getPrice());
        body += String.format("%010d", Integer.parseInt(payment.getVat()));
        body += String.format("%-20s", "");

        // 카드번호, 유효기간, cvc데이터를 안전하게 암호화한 값을 넘겨야함
        String info = cardNumber + "|" + duration + "|" + cvc;
        AES256 aes256 = new AES256();
        String encryptedInfo = aes256.encrypt(info);
        body += String.format("%-300s", encryptedInfo);
        body += String.format("%-47s", "");

        //공통헤더부문
        String dataType = String.format("%-10s", "PAYMENT");
        int dl = body.length() + dataType.length() + id.length();
        String dataLength = String.format("%4d", dl);
        String header = dataLength + dataType + id;
        // 전체 데이터
        String data = header + body;
        return new Payment(id, data);
    }

    private PaymentDetailDto decryptPayment(Payment payment) throws Exception {
        String data = payment.getData();
        String header = data.substring(0, 34);
        String dataLength = header.substring(0, 4).strip();
        String dataType = header.substring(4, 14).strip();
        String paymentId = header.substring(14).strip();
        String body = data.substring(34);
        String cardNumber = body.substring(0, 20).strip();
        String installment = body.substring(20, 22).strip();
        String duration = body.substring(22, 26).strip();
        String cvc = body.substring(26, 29).strip();
        String price = body.substring(29, 39).strip();
        String vat = body.substring(39, 49).replaceFirst("^0+(?!$)", "");
        String info = body.substring(69, 349).strip();
        AES256 aes256 = new AES256();
        String decryptedInfo = aes256.decrypt(info);
        String[] cardInfo = decryptedInfo.split("\\|");

        return new PaymentDetailDto(paymentId, cardNumber, duration, cvc, dataType, price, vat);
    }

    // 암호화된 결제데이터 + 취소요청데이터 -> 암호화
    private Payment encryptPaymentCancel(PaymentDetailDto paymentDetailDto, String id, PaymentCancelDto.Request paymentCancelReq) throws Exception {
        String body = "";
        // 데이터 부문
        String cardNumber = String.format("%-20s", paymentDetailDto.getCardNumber());
        String duration = String.format("%-4s", paymentDetailDto.getDuration());
        String cvc = String.format("%-3s", paymentDetailDto.getCvc());
        body += cardNumber;
        body += "00";
        body += duration;
        body += cvc;
        body += String.format("%10s", paymentCancelReq.getPrice());
        body += String.format("%010d", Integer.parseInt(paymentCancelReq.getVat()));
        body += String.format("%-20s", paymentDetailDto.getPaymentId());

        // 카드번호, 유효기간, cvc데이터를 안전하게 암호화한 값을 넘겨야함
        String info = cardNumber + "|" + duration + "|" + cvc;
        AES256 aes256 = new AES256();
        String encryptedInfo = aes256.encrypt(info);
        body += String.format("%-300s", encryptedInfo);
        body += String.format("%-47s", "");

        //공통헤더부문
        String dataType = String.format("%-10s", "CANCEL");
        int dl = body.length() + dataType.length() + id.length();
        String dataLength = String.format("%4d", dl);
        String header = dataLength + dataType + id;
        // 전체 데이터
        String data = header + body;
        return new Payment(id, data);
    }

    private Boolean validateVat(String paymentId){

        return true;
    }

/*
    private String sortData(String data, int length, String typ){
        String blank = "";
        String zero = "";
        int blankLength = length - data.length();
        for (int i = 0; i < blankLength; i++) {
            blank += "_";
            zero += "0";
        }
        if (typ.equals("num")){
            // 우측정렬 빈자리
            return blank + data;
        } else if (typ.equals("num0")) {
            // 우측정렬 빈자리 0
            return zero + data;
        } else {
            // 좌측정렬 빈자리 _
            return data + blank;
        }
    }
 */
}
