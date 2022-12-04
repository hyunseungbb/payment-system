package boys.payment.entity;

import lombok.Getter;

import javax.persistence.*;

@Getter
@SequenceGenerator(
        name = "SEQ_PAYMENT_GENERATER",
        sequenceName = "SEQ_PAYMENT",
        initialValue = 1,
        allocationSize = 1
)
@Entity
public class PaymentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PAYMENT_GENERATER")
    private Long id;

    public PaymentSequence(){}
}
