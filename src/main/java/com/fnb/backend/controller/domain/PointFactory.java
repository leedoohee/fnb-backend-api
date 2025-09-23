package com.fnb.backend.controller.domain;

import com.fnb.backend.controller.domain.implement.DiscountPolicy;
import com.fnb.backend.controller.domain.implement.PointPolicy;

public class PointFactory {
    public static PointPolicy getPolicy(String calculateType) {
        if ("P".equals(calculateType)) {
            return new RatePoint();
        } else if ("A".equals(calculateType)) {
            return new AbsolutePoint();
        }
        return null; // 또는 기본 전략 반환
    }
}