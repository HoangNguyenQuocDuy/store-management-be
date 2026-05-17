package com.seveneleven.service;

import com.seveneleven.dto.PageResponse;
import com.seveneleven.dto.order.CreateOrderRequest;
import com.seveneleven.dto.order.OrderResponse;
import com.seveneleven.dto.order.UpdateOrderStatusRequest;
import com.seveneleven.entity.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, String username);

    PageResponse<OrderResponse> getMyOrders(String username, OrderStatus status, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    PageResponse<OrderResponse> getAllOrders(OrderStatus status, LocalDate dateFrom, LocalDate dateTo, String userEmail, Pageable pageable);

    OrderResponse getById(Long id);

    OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request);

}
