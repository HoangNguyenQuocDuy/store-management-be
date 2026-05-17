package com.seveneleven.dto.order;

import com.seveneleven.entity.Order;
import com.seveneleven.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderResponse {

    private Long id;

    private Long userId;

    private String userFullName;

    private String userEmail;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private String shippingAddress;

    private String note;

    private List<OrderItemResponse> items;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUser().getId())
                .userFullName(o.getUser().getFullName())
                .userEmail(o.getUser().getEmail())
                .status(o.getStatus())
                .totalAmount(o.getTotalAmount())
                .shippingAddress(o.getShippingAddress())
                .note(o.getNote())
                .items(o.getItems() == null ? List.of() :
                        o.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList()))
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

}
