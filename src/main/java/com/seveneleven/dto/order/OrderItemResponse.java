package com.seveneleven.dto.order;

import com.seveneleven.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {

    private Long id;

    private Long productId;

    private String productName;

    private String productImageUrl;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

}
