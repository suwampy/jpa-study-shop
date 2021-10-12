package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name= "dtype")
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy="items")
    private List<Category> categoryies = new ArrayList<>();

    // == 비즈니스 로직 == //
    // 데이터를 가지고 있는 엔티티에서 비즈니스 로직이 나가게~~~~객체지향개발^_^
    /**
     * stock 증가
     * */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     * **/
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock <0) {
            throw new NotEnoughStockException("need mor stock");
        }
        this.stockQuantity = restStock;
    }
}
