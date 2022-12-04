package boys.payment.repository;

import boys.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import javax.swing.text.html.Option;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByPaymentId(String paymentId);

    @Query(value = "SELECT nextval('seq_payment')", nativeQuery = true)
    String getIdFromSeq();
    @Query(value = "SELECT * FROM Payment WHERE INSTR(payment.data, :paymentId, 1) = 84", nativeQuery = true)
    Iterable<Payment> findAllByPaymentIdInData(@Param("paymentId") String paymentId);
}
