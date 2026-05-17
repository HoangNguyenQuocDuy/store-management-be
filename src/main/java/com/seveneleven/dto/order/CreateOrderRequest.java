package com.seveneleven.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateOrderRequest {

    @NotBlank
    private String shippingAddress;

    private String note;

    @NotEmpty @Valid
    private List<OrderItemRequest> items;

}
