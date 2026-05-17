package com.seveneleven.service.impl;

import com.seveneleven.dto.PageResponse;
import com.seveneleven.dto.order.CreateOrderRequest;
import com.seveneleven.dto.order.OrderItemRequest;
import com.seveneleven.dto.order.OrderResponse;
import com.seveneleven.dto.order.UpdateOrderStatusRequest;
import com.seveneleven.entity.Order;
import com.seveneleven.entity.OrderItem;
import com.seveneleven.entity.OrderStatus;
import com.seveneleven.entity.Product;
import com.seveneleven.entity.User;
import com.seveneleven.exception.BadRequestException;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.repository.OrderRepository;
import com.seveneleven.repository.ProductRepository;
import com.seveneleven.repository.UserRepository;
import com.seveneleven.repository.spec.OrderSpec;
import com.seveneleven.service.NotificationService;
import com.seveneleven.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        log.info("#OrderServiceImpl.createOrder - START - username: {}, items: {}", username, request.getItems().size());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product is not found: " + itemReq.getProductId()));

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .build());
        }

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .note(request.getNote())
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));
        OrderResponse response = OrderResponse.from(orderRepository.save(order));

        log.info("#OrderServiceImpl.createOrder - created order #{}, total: {}", response.getId(), response.getTotalAmount());
        notificationService.notifyOrderCreated(response);

        return response;
    }

    @Override
    public PageResponse<OrderResponse> getMyOrders(String username, OrderStatus status, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.info("#OrderServiceImpl.getMyOrders - START - username: {}, status: {}, dateFrom: {}, dateTo: {}", username, status, dateFrom, dateTo);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User is not found!"));

        OffsetDateTime from = dateFrom != null ? dateFrom.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;

        Specification<Order> spec = Specification.where(OrderSpec.hasUserId(user.getId()))
                .and(OrderSpec.hasStatus(status))
                .and(OrderSpec.fromDate(from))
                .and(OrderSpec.toDate(to));
        PageResponse<OrderResponse> result = PageResponse.of(orderRepository.findAll(spec, pageable).map(OrderResponse::from));
        log.info("#OrderServiceImpl.getMyOrders - found {} orders for username: {}", result.getTotalElements(), username);

        return result;
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, LocalDate dateFrom, LocalDate dateTo, String userEmail, Pageable pageable) {
        log.info("#OrderServiceImpl.getAllOrders - START - status: {}, dateFrom: {}, dateTo: {}, userEmail: {}", status, dateFrom, dateTo, userEmail);

        OffsetDateTime from = dateFrom != null ? dateFrom.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59).atOffset(ZoneOffset.UTC) : null;

        Specification<Order> spec = Specification.where(OrderSpec.hasStatus(status))
                .and(OrderSpec.fromDate(from))
                .and(OrderSpec.toDate(to))
                .and(OrderSpec.hasUserEmail(userEmail));
        PageResponse<OrderResponse> result = PageResponse.of(orderRepository.findAll(spec, pageable).map(OrderResponse::from));
        log.info("#OrderServiceImpl.getAllOrders - totalElements: {}", result.getTotalElements());

        return result;
    }

    @Override
    public OrderResponse getById(Long id) {
        log.info("#OrderServiceImpl.getById - START - id: {}", id);

        OrderResponse response = OrderResponse.from(findOrder(id));
        log.info("#OrderServiceImpl.getById - found order #{}, status: {}", response.getId(), response.getStatus());

        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        log.info("#OrderServiceImpl.updateStatus - START - id: {}, newStatus: {}", id, request.getStatus());

        Order order = findOrder(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order status cannot be changed once it has been confirmed or rejected");
        }

        if (request.getStatus() == OrderStatus.CONFIRMED) {
            List<Product> productsToUpdate = new ArrayList<>();
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + product.getName());
                }
                productsToUpdate.add(product);
            }
            for (int i = 0; i < order.getItems().size(); i++) {
                Product p = productsToUpdate.get(i);
                p.setStockQuantity(p.getStockQuantity() - order.getItems().get(i).getQuantity());
            }
            productRepository.saveAll(productsToUpdate);
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.getStatus());
        OrderResponse response = OrderResponse.from(orderRepository.save(order));

        log.info("#OrderServiceImpl.updateStatus - order #{} status: {} → {}", id, previousStatus, response.getStatus());
        notificationService.notifyOrderStatusChanged(response, previousStatus);

        return response;
    }

    private Order findOrder(Long id) {
        log.info("#OrderServiceImpl.findOrder - START - id: {}", id);

        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order is not found!"));
    }
}
