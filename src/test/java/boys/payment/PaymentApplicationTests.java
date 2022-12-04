package boys.payment;

import boys.payment.dto.PaymentCancelDto;
import boys.payment.dto.PaymentDto;
import boys.payment.entity.Payment;
import boys.payment.repository.PaymentRepository;
import boys.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class PaymentApplicationTests {

	@Autowired
	PaymentService paymentService;
	@Autowired
	PaymentRepository paymentRepository;

	@Test
	public void payment() throws Exception {
		PaymentDto.Request paymentReq = new PaymentDto.Request(
				"1111222233334444",
				"1225",
				"636",
				"6",
				"11000",
				"1000"
		);
		PaymentDto.Response res = paymentService.savePayment(paymentReq).getBody();
		Payment payment = paymentRepository.findById(res.getPaymentId()).orElseGet(null);
		assertThat(res.getData()).isEqualTo(payment.getData());
	}

	@Test
	public void paymentCancel() throws Exception {

	}

	@Test
	public void getPayment() throws Exception {

	}

	@Test
	public void case1() throws Exception {
		PaymentDto.Request paymentReq = new PaymentDto.Request(
				"1111222233334444",
				"1225",
				"636",
				"6",
				"11000",
				"1000"
		);
		String id = paymentService.savePayment(paymentReq).getBody().getPaymentId();
		PaymentCancelDto.Request cancelReq1 = new PaymentCancelDto.Request(
				id,
				"1100",
				"100"
		);
		PaymentCancelDto.Request cancelReq2 = new PaymentCancelDto.Request(
				id,
				"3300",
				null
		);
		PaymentCancelDto.Request cancelReq3 = new PaymentCancelDto.Request(
				id,
				"7000",
				null
		);
		PaymentCancelDto.Request cancelReq4 = new PaymentCancelDto.Request(
				id,
				"6600",
				"700"
		);
		PaymentCancelDto.Request cancelReq5 = new PaymentCancelDto.Request(
				id,
				"6600",
				"600"
		);
		PaymentCancelDto.Request cancelReq6 = new PaymentCancelDto.Request(
				id,
				"100",
				null
		);
		HttpStatus httpStatus1 = paymentService.savePaymentCancel(cancelReq1).getStatusCode();
		HttpStatus httpStatus2 = paymentService.savePaymentCancel(cancelReq2).getStatusCode();
		HttpStatus httpStatus3 = paymentService.savePaymentCancel(cancelReq3).getStatusCode();
		HttpStatus httpStatus4 = paymentService.savePaymentCancel(cancelReq4).getStatusCode();
		HttpStatus httpStatus5 = paymentService.savePaymentCancel(cancelReq5).getStatusCode();
		HttpStatus httpStatus6 = paymentService.savePaymentCancel(cancelReq6).getStatusCode();

		assertThat(httpStatus1).isEqualTo(HttpStatus.OK);
		assertThat(httpStatus2).isEqualTo(HttpStatus.OK);
		assertThat(httpStatus3).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(httpStatus4).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(httpStatus5).isEqualTo(HttpStatus.OK);
		assertThat(httpStatus6).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void case2() throws Exception {
		PaymentDto.Request paymentReq = new PaymentDto.Request(
				"1111222233334444",
				"1225",
				"636",
				"6",
				"20000",
				"909"
		);
		String id = paymentService.savePayment(paymentReq).getBody().getPaymentId();
		PaymentCancelDto.Request cancelReq1 = new PaymentCancelDto.Request(
				id,
				"10000",
				"0"
		);
		PaymentCancelDto.Request cancelReq2 = new PaymentCancelDto.Request(
				id,
				"10000",
				"0"
		);
		PaymentCancelDto.Request cancelReq3 = new PaymentCancelDto.Request(
				id,
				"10000",
				"909"
		);
		HttpStatus httpStatus1 = paymentService.savePaymentCancel(cancelReq1).getStatusCode();
		HttpStatus httpStatus2 = paymentService.savePaymentCancel(cancelReq2).getStatusCode();
		HttpStatus httpStatus3 = paymentService.savePaymentCancel(cancelReq3).getStatusCode();

		assertThat(httpStatus1).isEqualTo(HttpStatus.OK);
		assertThat(httpStatus2).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(httpStatus3).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void case3() throws Exception {
		PaymentDto.Request paymentReq = new PaymentDto.Request(
				"1111222233334444",
				"1225",
				"636",
				"6",
				"20000",
				null
		);
		String id = paymentService.savePayment(paymentReq).getBody().getPaymentId();
		PaymentCancelDto.Request cancelReq1 = new PaymentCancelDto.Request(
				id,
				"10000",
				"1000"
		);
		PaymentCancelDto.Request cancelReq2 = new PaymentCancelDto.Request(
				id,
				"10000",
				"909"
		);
		PaymentCancelDto.Request cancelReq3 = new PaymentCancelDto.Request(
				id,
				"10000",
				null
		);
		HttpStatus httpStatus1 = paymentService.savePaymentCancel(cancelReq1).getStatusCode();
		HttpStatus httpStatus2 = paymentService.savePaymentCancel(cancelReq2).getStatusCode();
		HttpStatus httpStatus3 = paymentService.savePaymentCancel(cancelReq3).getStatusCode();

		assertThat(httpStatus1).isEqualTo(HttpStatus.OK);
		assertThat(httpStatus2).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(httpStatus3).isEqualTo(HttpStatus.OK);
	}

	@Test
	void testConcurrency1() throws Exception {
		PaymentDto.Request paymentReq = new PaymentDto.Request(
				"1111222233334444",
				"1225",
				"636",
				"6",
				"20000",
				"1000"
		);
		String id = paymentService.savePayment(paymentReq).getBody().getPaymentId();
		PaymentCancelDto.Request cancelReq = new PaymentCancelDto.Request();
		cancelReq.setPaymentId(id);
		cancelReq.setPrice("10000");
		cancelReq.setVat("500");

		int numberOfThreads = 3;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			int finalI = i;
			executorService.submit(() -> {
				HttpStatus httpStatus = null;
				try {
					httpStatus = paymentService.savePaymentCancel(cancelReq).getStatusCode();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
//					assertThat(httpStatus).isEqualTo(HttpStatus.OK);
				}
			});
		}
		latch.await();
		System.out.println("전체");
		Iterable<Payment> payments = paymentRepository.findAll();
		for(Payment payment: payments) {
			System.out.println(payment.getPaymentId());
		}
		System.out.println("취소내역");
		payments = paymentRepository.findAllByPaymentIdInData(id);
		for (Payment payment: payments) {
			System.out.println(payment.getPaymentId());
		}
	}

	@Test
	void testConcurrency2() throws Exception {
		PaymentDto.Request paymentReq = new PaymentDto.Request(
				"1111222233334444",
				"1225",
				"636",
				"6",
				"20000",
				"909"
		);
		String id = paymentService.savePayment(paymentReq).getBody().getPaymentId();
		PaymentCancelDto.Request cancelReq = new PaymentCancelDto.Request();
		cancelReq.setPaymentId(id);
		cancelReq.setPrice("20000");
		cancelReq.setVat("909");

		int numberOfThreads = 3;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			executorService.submit(() -> {
				HttpStatus httpStatus = null;
				try {
					httpStatus = paymentService.savePaymentCancel(cancelReq).getStatusCode();
				} catch (Exception e) {
					e.printStackTrace();
				}
				assertThat(httpStatus).isEqualTo(HttpStatus.OK);
				latch.countDown();
			});
		}
		latch.await();
		Iterable<Payment> payments = paymentRepository.findAll();
		for(Payment payment: payments) {
			System.out.println(payment.getPaymentId());
		}
	}
	@Test
	void contextLoads() {
	}

}
