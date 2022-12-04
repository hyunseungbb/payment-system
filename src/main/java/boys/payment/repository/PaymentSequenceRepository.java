package boys.payment.repository;

import boys.payment.entity.PaymentSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSequenceRepository extends JpaRepository<PaymentSequence, Long> {
}
