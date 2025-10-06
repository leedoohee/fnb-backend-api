package com.fnb.front.backend.controller.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@Entity
@Builder
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "coupon_type")
    private String couponType;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "apply_start_at")
    private Date applyStartAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "apply_end_at")
    private Date applyEndAt;

    @Column(name = "min_apply_price", precision = 19, scale = 2)
    private BigDecimal minApplyPrice;

    @Column(name = "member_ship_grades")
    private String memberShipGrades;

    @Column(name = "available_quantity")
    private int availableQuantity;

    @Column(name = "used_quantity")
    private int usedQuantity;

    @Column(name = "status")
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "issued_type") // 자동 발급, 수동 발급
    private String issuedType;

    @Column(name = "apply_grades")
    private String applyGrades;

    @Transient
    private int applyProductId;

    @Transient
    private List<CouponProduct> couponProducts;

    @Transient
    private List<MemberCoupon> memberCoupons;

    public Coupon() {

    }

    public boolean isCanApplyDuring() {
        Date now = new Date();
        return this.applyStartAt.after(now) && this.applyEndAt.before(now);
    }

    public boolean isAvailableStatus() {
        return this.status.equals("1");
    }

    public boolean isBelongToAvailableGrade(Member member) {
        List<String> grades = Arrays.stream(applyGrades.split(",")).toList();
        return grades.contains(member.getGrade());
    }
}
