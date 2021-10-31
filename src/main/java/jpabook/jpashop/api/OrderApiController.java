package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
      List<Order> all =orderRepository.findAllByString(new OrderSearch());
      for (Order order : all) {
          order.getMember().getName();
          order.getDelivery().getAddress();
          List<OrderItem> orderItems = order.getOrderItems();
          orderItems.stream().forEach(o-> o.getItem().getName());
      }
      return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch()) ;
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3(){
        /**
         *         return em.createQuery(
         *                 "select distinct o from Order o" +
         *                         " join fetch o.member m" +
         *                         " join fetch o.delivery d" +
         *                         " join fetch o.orderItems oi" +
         *                         " join fetch oi.item i", Order.class)
         *                 .getResultList();
         *
         * -> 패치 조인으로 sQL이 1번만 실행됨
         * distinct를 사용한 이유는 1대 다 조인이 있으므로 데이터베이스 row가 증가
         * 그 결과 같은 order 엔티티의 조회 수도 증가하게 됨
         * JPA 의 distinct 는 SQL 에 distinct 를 추가하고 더해서 같은 엔티티가 조회되면
         * 애플리케이션에서 중복을 걸러줌
         * 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아줌
         * 단점 -> 페이징 불가
         * */
       List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;

    }

    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        // vo는.. 노출여부 상관업음ㅋㅎㅋㅎ
        private Address address;

        // orderItem도 Dto로 바꿔야한다 !!
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName; // 상품명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
