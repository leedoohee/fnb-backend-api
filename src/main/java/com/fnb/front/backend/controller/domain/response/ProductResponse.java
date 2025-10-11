package com.fnb.front.backend.controller.domain.response;


import com.fnb.front.backend.controller.domain.ProductAttachFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductResponse {

    private int id;
    private String name;
    private String img;
    private String description;
    private String merchantId;
    private BigDecimal price;
    private String status;
    private String type;
    private String category;
    private int isAvailableCoupon;
    private int purchaseQuantity;
    private int minPurchaseQuantity;
    private int maxPurchaseQuantity;
    private int isApplyMemberShip;
    private String applyMemberGrades;
    private String applyMemberGradeDisType;
    private BigDecimal applyMemberGradeDisAmt;
    private int reviewCount;

    List<ProductAttachFile> productAttachFiles;
    List<ProductOptionResponse> productOptions;
}
