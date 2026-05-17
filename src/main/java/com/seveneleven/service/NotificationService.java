package com.seveneleven.service;

import com.seveneleven.dto.order.OrderResponse;
import com.seveneleven.entity.OrderStatus;

public interface NotificationService {

    void notifyOrderCreated(OrderResponse order);

    void notifyOrderStatusChanged(OrderResponse order, OrderStatus previousStatus);

}
