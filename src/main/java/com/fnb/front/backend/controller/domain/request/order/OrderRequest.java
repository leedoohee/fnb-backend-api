package com.fnb.front.backend.controller.domain.request.order;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private String memberId;
    private int orderType;
    private String merchantId;
    private BigDecimal point;
    private List<OrderProductRequest> orderProductRequests;
    private List<OrderCouponRequest> orderCouponRequests;
}
