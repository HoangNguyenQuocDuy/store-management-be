package com.seveneleven.controller;

import com.seveneleven.dto.ApiResponse;
import com.seveneleven.dto.PageResponse;
import com.seveneleven.dto.order.CreateOrderRequest;
import com.seveneleven.dto.order.OrderResponse;
import com.seveneleven.dto.order.UpdateOrderStatusRequest;
import com.seveneleven.entity.OrderStatus;
import com.seveneleven.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("#OrderController.create - START - username: {}, items: {}", userDetails.getUsername(), request.getItems().size());

        ApiResponse<OrderResponse> response = ApiResponse.ok("Order created successfully",
                orderService.createOrder(request, userDetails.getUsername()));
        log.info("#OrderController.create - created order #{}", response.getData().getId());

        return response;
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String userEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("#OrderController.list - START");

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (isAdmin(userDetails)) {
            log.info("#OrderController.list - ADMIN listing all orders, status: {}, userEmail: {}", status, userEmail);
            return ApiResponse.ok(orderService.getAllOrders(status, dateFrom, dateTo, userEmail, pageable));
        }

        log.info("#OrderController.list - USER listing own orders for: {}", userDetails.getUsername());

        return ApiResponse.ok(orderService.getMyOrders(userDetails.getUsername(), status, dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> detail(@PathVariable Long id) {
        log.info("#OrderController.detail - START - id: {}", id);
        return ApiResponse.ok(orderService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateOrderStatusRequest request) {
        log.info("#OrderController.updateStatus - START - id: {}, status: {}", id, request.getStatus());

        ApiResponse<OrderResponse> response = ApiResponse.ok("Status updated successfully",
                orderService.updateStatus(id, request));
        log.info("#OrderController.updateStatus - updated order #{} to {}", id, request.getStatus());

        return response;
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
