package com.seveneleven.dto.order;

import com.seveneleven.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus status;

}
