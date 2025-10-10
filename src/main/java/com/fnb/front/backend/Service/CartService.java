package com.fnb.front.backend.Service;

import com.fnb.front.backend.controller.domain.*;
import com.fnb.front.backend.controller.domain.request.order.CartInfoResponse;
import com.fnb.front.backend.controller.domain.request.order.CartItemRequest;
import com.fnb.front.backend.controller.domain.request.order.CartRequest;
import com.fnb.front.backend.controller.domain.request.order.OptionInfoResponse;
import com.fnb.front.backend.repository.CartRepository;
import com.fnb.front.backend.repository.MemberRepository;
import com.fnb.front.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional
    public boolean create(CartRequest cartRequest) {
        Cart cart = this.cartRepository.findCart(cartRequest.getMemberId());

        if (cart != null) {
            this.cartRepository.deleteCart(cart.getId());
            this.cartRepository.deleteCartItem(cart.getId());
        }

        int cartId = this.cartRepository.insertCart(Cart.builder()
                            .memberId(cartRequest.getMemberId())
                            .productId(cartRequest.getProductId())
                            .createdAt(LocalDateTime.now())
                            .build());

        if (cartId > 0) {
            for (CartItemRequest cartItemRequest : cartRequest.getCartItemRequests()) {
                this.cartRepository.insertCartItem(CartItem.builder()
                            .cartId(cartId)
                            .optionId(cartItemRequest.getOptionId())
                            .optionType(cartItemRequest.getOptionType())
                            .optionGroupId(cartItemRequest.getOptionGroupId())
                            .createdAt(LocalDateTime.now())
                            .build());
            }
        }

        return true;
    }

    public CartInfoResponse getInfo(String memberId) {
        Cart cart = this.cartRepository.findCart(memberId);
        List<OptionInfoResponse> optionInfoResponses = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            optionInfoResponses.add(OptionInfoResponse.builder()
                    .optionGroupId(cartItem.getOptionGroupId())
                    .price(cartItem.getProductOption().getPrice())
                    .optionName(cartItem.getProductOption().getName())
                    .optionId(cartItem.getOptionId())
                    .build());
        }

        return CartInfoResponse.builder()
                .minQuantity(cart.getProduct().getMinQuantity())
                .maxQuantity(cart.getProduct().getMaxQuantity())
                .productId(cart.getProduct().getId())
                .description(cart.getProduct().getDescription())
                .productName(cart.getProduct().getName())
                .address(cart.getMember().getAddress())
                .memberId(cart.getMember().getMemberId())
                .cartId(cart.getId())
                .options(optionInfoResponses)
                .build();
    }
}
