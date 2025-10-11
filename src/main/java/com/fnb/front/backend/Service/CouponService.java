package com.fnb.front.backend.Service;

import com.fnb.front.backend.controller.domain.*;
import com.fnb.front.backend.controller.domain.response.CouponResponse;
import com.fnb.front.backend.repository.CouponRepository;
import com.fnb.front.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    public CouponService(CouponRepository couponRepository, MemberRepository memberRepository) {
        this.couponRepository = couponRepository;
        this.memberRepository = memberRepository;
    }

    public List<CouponResponse> getCoupons() {
        List<CouponResponse> responses  = new ArrayList<>();
        List<Coupon> coupons            = this.couponRepository.findCoupons();

        for (Coupon coupon : coupons) {
            responses.add(CouponResponse.builder()
                            .couponType(coupon.getCouponType())
                            .applyStartAt(coupon.getApplyStartAt())
                            .couponName(coupon.getName())
                            .description(coupon.getDescription())
                            .couponId(coupon.getId())
                            .status(coupon.getStatus())
                            .applyEndAt(coupon.getApplyEndAt())
                            .build());
        }

        return responses;
    }

    public boolean createMemberCoupon(String memberId, int couponId) {
        Member member               = this.memberRepository.findMember(memberId);
        Coupon coupon               = this.couponRepository.findCoupon(couponId);
        MemberCoupon memberCoupon   = this.couponRepository.findMemberCoupon(member.getMemberId(), couponId);

        assert memberCoupon != null : "이미 소유한 쿠폰입니다";

        assert (coupon != null && !isUsableCoupon(coupon, member)): "소유할 수 없는 쿠폰입니다.";

        MemberCoupon mCoupon = MemberCoupon.builder()
                .memberId(memberId)
                .isUsed("1")
                .createdAt(LocalDateTime.now())
                .couponId(couponId).build();

        int memberCouponId = this.couponRepository.insertMemberCoupon(mCoupon);

        assert memberCouponId <= 0 : "저장 과정중 오류가 발생하였습니다.";

        return true;
    }

    public boolean applyCouponToProduct(String memberId, int couponId, int productId) {
        MemberCoupon memberCoupon = this.couponRepository.findMemberCoupon(memberId, couponId);
        Coupon coupon             = memberCoupon.getCoupon();

        assert memberCoupon == null : "소유하지 않은 쿠폰입니다.";

        if(coupon.isApplyToEntireProduct()) {
            return true;
        }

        if (!coupon.isApplyToEntireProduct()) {
            CouponProduct couponProduct = memberCoupon.getCoupon().getCouponProducts().stream()
                    .filter(element -> element.getProductId() == productId).findFirst().orElse(null);

            assert couponProduct == null : "쿠폰적용이 불가능한 상품입니다.";
        }

        assert coupon != null && isUsableCoupon(coupon, memberCoupon.getMember()) : "적용불가능한 쿠폰입니다.";

        return true;
    }

    private boolean isUsableCoupon(Coupon coupon, Member member) {
        if (coupon.isAvailableStatus()) {
            return true;
        }

        if (coupon.isBelongToAvailableGrade(member)) {
            return true;
        }

        if (coupon.isCanApplyDuring()) {
            return true;
        }

        return false;
    }
}
