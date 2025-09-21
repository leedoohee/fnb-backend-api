package com.fnb.backend.controller.domain.processor;

import com.fnb.backend.controller.domain.*;
import com.fnb.backend.controller.domain.implement.DiscountPolicy;
import com.fnb.backend.controller.domain.implement.PriceCalculator;
import com.fnb.backend.controller.dto.CreateOrderDto;
import com.fnb.backend.controller.dto.CreateOrderProductDto;

import java.math.BigDecimal;
import java.util.*;

public class OrderProcessor {
    private final Member member;

    private final Order order;
    private final List<Product> products;
    private final List<Coupon> coupons;

    public OrderProcessor(Member member, Order order, List<Product> products, List<Coupon> coupons) {
        this.member = member;
        this.order = order;
        this.products = products;
        this.coupons = coupons;
    }

    public CreateOrderDto buildOrder() {

        if(!this.member.isCanPurchase()) {
            return null;
        }

        if(!this.validatePoint(this.order.getPoint(), this.member.getOwnedPointAmount())) {
            return null;
        }

        if(!this.isOwnedCoupons(this.member, this.coupons)){
            return null;
        }

        if(!this.validateCoupons(this.coupons, this.member)){
            return null;
        }

        if(!this.validateOrderProduct(this.products)) {
            return null;
        }

        return CreateOrderDto.builder()
                .orderId(this.generateOrderId(this.order.getMerchantId()))
                .orderDate(this.order.getOrderDate())
                .merchantId(this.order.getMerchantId())
                .orderProducts(this.buildOrderProducts(this.member, this.products, this.coupons, this.order.getPoint()))
                .build();
    }

    private boolean validatePoint(BigDecimal point, int ownedPoint) {
        return point.intValue() - ownedPoint <= 0;
    }

    private boolean validateCoupons(List<Coupon> coupons, Member member) {
        for(Coupon coupon : coupons) {
            if(!coupon.isAvailableStatus()) {
                return false;
            }

            if(!coupon.isCanApplyDuring()) {
                return false;
            }

            if(!coupon.isBelongToAvailableGrade(member)) {
                return false;
            }
        }

        return true;
    }

    private boolean isOwnedCoupons(Member member, List<Coupon> coupons) {
        List<MemberCoupon> ownedCoupons = member.getOwnedCoupon();
        List<Integer> ownedCouponIds = ownedCoupons.stream().map(MemberCoupon::getCouponId).toList();

        for (Coupon coupon : coupons) {
            if(!ownedCouponIds.contains(coupon.getId())) {
                return false;
            }
        }

        return true;
    }

    private boolean validateOrderProduct(List<Product> products) {

        for (Product product : products) {
            if(!product.isAvailablePurchase()) {
                return false;
            }

            if(product.isLessMinPurchaseQuantity()) {
                return false;
            }

            if(product.isOverMaxPurchaseQuantity()) {
                return false;
            }
        }

        return true;
    }

    private List<CreateOrderProductDto> buildOrderProducts(Member member, List<Product> products, List<Coupon> coupons, BigDecimal usePoint) {
        List<CreateOrderProductDto> createOrderProductDtos = new ArrayList<>();

        int dividedPoint = Math.round(usePoint.floatValue() / products.size());

        for (Product product : products) {
            int memberShipPrice = 0;

            if(!product.isAvailableUseCoupon()) {
                return null;
            }

            if (product.inMemberShipDiscount(member)) {
                memberShipPrice = this.calculateMemberShipToProduct(product, member);
            }

            int couponPrice = this.calculateCouponToProduct(product, coupons);

            CreateOrderProductDto orderProduct = CreateOrderProductDto.builder()
                    .orderId(this.generateOrderId(this.order.getMerchantId()))
                    .orderProductId(this.generateOrderProductId(product.getMerchantId(), String.valueOf(product.getId())))
                    .productId(product.getId())
                    .name(product.getName())
                    .couponId(this.applyCouponToProduct(product, coupons))
                    .quantity(product.getPurchaseQuantity())
                    .originPrice(product.getTotalPrice())
                    .purchasePrice(product.getTotalPrice() - couponPrice - memberShipPrice - dividedPoint)
                    .couponPrice(couponPrice)
                    .memberShipPrice(memberShipPrice)
                    .point(dividedPoint)
                    .discountPrice(couponPrice + memberShipPrice + dividedPoint)
                    .build();

            createOrderProductDtos.add(orderProduct);
        }

        return createOrderProductDtos;
    }

    private int applyCouponToProduct(Product product, List<Coupon> coupons) {
        return coupons.stream()
                .filter(coupon -> Objects.equals(coupon.getApplyProductId(), product.getId()))
                .map(Coupon::getId)
                .mapToInt(Integer::intValue).findFirst().orElse(0);
    }

    private int calculateCouponToProduct(Product product, List<Coupon> coupons) {
        return coupons.stream()
            .filter(coupon -> Objects.equals(coupon.getApplyProductId(), product.getId() ))
            .map(coupon -> {

                DiscountPolicy couponPolicy = DiscountFactory.getPolicy(coupon.getDiscountType());
                CouponPriceCalculator couponPriceCalculator = null;

                if (couponPolicy != null) {
                    couponPriceCalculator = new CouponPriceCalculator(coupon, product, couponPolicy);
                }

                return Objects.requireNonNull(couponPriceCalculator).calculatePrice();
            }).mapToInt(BigDecimal::intValue).findFirst().orElse(0);
    }

    private int calculateMemberShipToProduct(Product product, Member member) {
        PriceCalculator memberShipCalculator = null;

        DiscountPolicy memberShipPolicy = DiscountFactory.getPolicy(product.getApplyMemberGradeDisType());

        if (memberShipPolicy != null) {
            memberShipCalculator = new MemberShipCalculator(member, product, memberShipPolicy);
        }

        return Objects.requireNonNull(memberShipCalculator).calculatePrice().intValue();
    }

    private String generateOrderId(String merchantId) {
        return "ORDER_" + merchantId + "_" + new Date().getTime();
    }

    private String generateOrderProductId(String merchantId, String productId) {
        return "ORDER_" + merchantId + "_" + productId + "_"  + new Date().getTime();
    }
}
