package boys.payment.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Data
public class Payment {
    @Id
    private String paymentId;
    @Column(length = 1000)
    private String data;

    public Payment(){}

    public Payment(String paymentId, String data){
        this.paymentId = paymentId;
        this.data = data;
    }
}
