package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * XToOne(ManyToOne, OneToOne)
 * Order 주문
 * Order 주문 -> Member 회원
 * Order 주문 -> Delivery 배송
 * */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    /**
     * 1. entity 를 그대로 반환 (이렇게 하면 안됨 ^_^)
     * */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        //  모든 order(주문) 을 반환한다
        /* *
         * => 양쪽을 서로 호출하면서 무한 루프에 빠짐!
         * Order -> Member-> Orders -> Member …
         * 양방향 관계 시 JsonIgnore 설정을 해줘야한다
         * */
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

       /**
        * 지연 로딩 (LAZY) 를 피가히 위해 즉시 로딩 (EAGER)로 설정하면 안됨
        * 항상 지연 로딩을 기본으로 하고 성능 최적화가 필요한 경우네느
        * 페치 조인을 사용하자
        * */
       for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;

        /**
         * 이대로 return 하면?
         * 나는 주문에 관련된 정보만 얻고 싶은데
         * order entity 에 있는 orderItem 의 정보까지 return 되어
         * order 와 관련된 모든 entity 에 대한 쿼리가 나가게 되고....
         * 성능상으로도 안좋은 영향을 끼침
         * * */
    }

    /**
     * 2. 엔티티를 DTO로 반환
     * */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        /*
         * Lazy 로딩으로 인한 불필요한 entity 호출 문제
         * 테이블 3개가 조회됨..
         * order, delivery
         * */

        /*
         * 쿼리가 총 1+ N + N번 실행된다
         * N + 1 -> 1 + 회원 N + 배송 N
         * order 조회 1번
         * order -> member 지연 로딩 조회 N번
         * order -> delivery 지연 로딩 조회 N번
         * */
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // DTO 타입으로 바꾸자
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    /*
    * 3. fetch 조인으로 성능 최적화
         select o from Order o
         join fetch o.member m
        join fetch o.delivery d
    * 쿼리가 한 번 나감!!
    * */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o->new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }



   @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}
