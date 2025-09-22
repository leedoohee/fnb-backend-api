package com.fnb.backend.Service;

import com.fnb.backend.controller.domain.*;
import com.fnb.backend.controller.domain.response.OrderResponse;
import com.fnb.backend.repository.CouponRepository;
import com.fnb.backend.repository.MemberRepository;
import com.fnb.backend.repository.OrderRepository;
import com.fnb.backend.repository.ProductRepository;
import com.fnb.backend.controller.domain.processor.OrderProcessor;
import com.fnb.backend.controller.dto.CreateOrderDto;
import com.fnb.backend.controller.dto.CreateOrderProductDto;
import com.fnb.backend.controller.request.order.OrderCouponRequest;
import com.fnb.backend.controller.request.order.OrderProductRequest;
import com.fnb.backend.controller.request.order.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Transactional
    public OrderResponse process(OrderRequest orderRequest) {
        Order order                 = this.createOrder(orderRequest);
        List<Product> orderProducts = this.createOrderProduct(orderRequest);
        List<Coupon> orderCoupons   = this.createOrderCoupon(orderRequest);
        Member member               = this.createMember(orderRequest);
        List<OrderProduct> newOrderProducts = new ArrayList<>();
        OrderProcessor orderProcessor       = new OrderProcessor(member, order, orderProducts, orderCoupons);
        CreateOrderDto createOrderDto       = orderProcessor.buildOrder();

        Order newOrder = Order.builder()
                .orderId(createOrderDto.getOrderId())
                .orderDate(createOrderDto.getOrderDate())
                .orderStatus("0")
                .orderType("1")
                .discountAmount(createOrderDto.getDiscountAmount())
                .useCouponAmount(createOrderDto.getCouponAmount())
                .paymentAmount(createOrderDto.getOrderAmount()).build();

        this.insertOrder(newOrder);

        for(CreateOrderProductDto element : createOrderDto.getOrderProducts()) {
            newOrderProducts.add(OrderProduct.builder()
                    .orderProductId(element.getOrderProductId())
                    .productId(element.getProductId())
                    .quantity(element.getQuantity())
                    .couponPrice(element.getCouponPrice())
                    .couponId(element.getCouponId())
                    .originPrice(element.getOriginPrice())
                    .discountPrice(element.getDiscountPrice())
                    .orderId(element.getOrderId())
                    .build());
        }

        this.insertOrderProducts(newOrderProducts);

        if(this.isNonExecutePaymentGateWay(createOrderDto.getOrderProducts())) {
            this.paymentService.insertPayments(createOrderDto.getOrderId(), null);
        } else {

        }

        return null;
    }

    public List<OrderProduct> getOrderProducts(String orderId) {
        return this.orderRepository.getOrderProducts(orderId);
    }

    public Order getOrder(String orderId) {
        return this.orderRepository.getOrder(orderId);
    }

    private boolean isNonExecutePaymentGateWay(List<CreateOrderProductDto> orderProductRequests) {
        return orderProductRequests.stream()
                .map(CreateOrderProductDto::getPurchasePrice)
                .mapToInt(Integer::intValue).sum() == BigDecimal.ZERO.intValue();
    }

    private Order createOrder(OrderRequest orderRequest) {
        return Order.builder()
                .orderType(orderRequest.getOrderType())
                .usePoint(orderRequest.getPoint())
                .orderDate(new Date())
                .merchantId(orderRequest.getMerchantId())
                .build();
    }

    private List<Product> createOrderProduct(OrderRequest orderRequest) {
        List<Product> orderProducts = new ArrayList<>();

        for (OrderProductRequest orderProductRequest : orderRequest.getOrderProductRequests()) {
            Product product = productRepository.find(orderProductRequest.getProductId());

            product.setProductOptions(productRepository.findOptionsById(product.getId(), orderProductRequest.getProductOptionId()));
            product.setPurchaseQuantity(orderProductRequest.getQuantity());
            orderProducts.add(product);
        }

        return orderProducts;
    }

    private List<Coupon> createOrderCoupon(OrderRequest orderRequest) {
        List<Integer> couponIds = orderRequest.getOrderCouponRequests().stream()
                            .map(OrderCouponRequest::getCouponId)
                            .toList();

        List<Coupon> coupons  = couponRepository.findInIds(couponIds.toString());

        for (Coupon coupon : coupons) {
            orderRequest.getOrderCouponRequests().stream()
                    .filter(orderCouponRequest -> Objects.equals(orderCouponRequest.getCouponId(), coupon.getId()))
                    .findFirst()
                    .ifPresent(orderCouponRequest -> { orderCouponRequest.setCouponId(coupon.getId()); });
        }

        return coupons;
    }

    private Member createMember(OrderRequest orderRequest) {
        Member member                       = memberRepository.find(orderRequest.getMemberId());
        List<MemberCoupon> memberCoupons    = memberRepository.findMemberCouponsById(member.getId());
        List<Point> points                  = memberRepository.findPointsById(member.getId());

        member.setPoints(points);
        member.setOwnedCoupon(memberCoupons);

        return member;
    }

    private void insertOrder(Order order) {
        this.orderRepository.insertOrder(order);
    }

    private void insertOrderProducts(List<OrderProduct> orderProducts) {
        this.orderRepository.insertOrderProducts(orderProducts);
    }
}
