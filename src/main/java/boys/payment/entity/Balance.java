package boys.payment.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
@Data
public class Balance {
    @Id
    private String paymentId;
    private Integer price;
    private Integer vat;
    @Version
    private Integer version;

    public Balance(){

    }
    public Balance(String paymentId, int price){
        this.paymentId = paymentId;
        this.price = price;
    }
}
