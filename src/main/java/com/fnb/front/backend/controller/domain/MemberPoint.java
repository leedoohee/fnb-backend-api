package com.fnb.front.backend.controller.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "member_point")
public class MemberPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id", updatable = false, nullable = false)
    private int pointId;

    @Column(name = "order_id", updatable = false, nullable = false)
    private String orderId;

    @Column(name = "point_type")
    private int pointType;

    @Column(name = "amount")
    private int amount;

    @Column(name = "is_used", nullable = false)
    private String isUsed;

    @Column(name = "member_id", updatable = false, nullable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable=false, updatable=false)
    private Member member;

    public MemberPoint() {

    }
}
