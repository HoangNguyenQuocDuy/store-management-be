package com.seveneleven.service.impl;

import com.seveneleven.config.AWSProperties;
import com.seveneleven.dto.order.OrderItemResponse;
import com.seveneleven.dto.order.OrderResponse;
import com.seveneleven.entity.OrderStatus;
import com.seveneleven.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final String BRAND_GREEN = "#16a34a";
    private static final String STORE_NAME = "QD Store";

    private final AWSProperties props;
    private final SesV2Client sesClient;
    private final TemplateEngine templateEngine;

    public NotificationServiceImpl(
            AWSProperties props,
            @Autowired(required = false) SesV2Client sesClient,
            TemplateEngine templateEngine) {
        this.props = props;
        this.sesClient = sesClient;
        this.templateEngine = templateEngine;
    }

    @Async("notificationExecutor")
    @Override
    public void notifyOrderCreated(OrderResponse order) {
        if (!props.isEnabled()) return;
        log.info("#NotificationServiceImpl.notifyOrderCreated - orderId: {}", order.getId());

        sendEmail(order.getUserEmail(),
                "[" + STORE_NAME + "] Xác nhận đơn hàng #" + order.getId(),
                templateEngine.process("email/order-confirmation", buildOrderContext(order)));
        sendEmail(props.getSes().getAdminEmail(),
                "[" + STORE_NAME + " Admin] Đơn hàng mới #" + order.getId() + " từ " + order.getUserEmail(),
                templateEngine.process("email/admin-alert", buildOrderContext(order)));
    }

    @Async("notificationExecutor")
    @Override
    public void notifyOrderStatusChanged(OrderResponse order, OrderStatus previousStatus) {
        if (!props.isEnabled()) return;
        log.info("#NotificationServiceImpl.notifyOrderStatusChanged - orderId: {}, {} → {}",
                order.getId(), previousStatus, order.getStatus());

        Context ctx = buildOrderContext(order);
        ctx.setVariable("previousStatusLabel", statusLabel(previousStatus));
        ctx.setVariable("previousStatusColor", statusColor(previousStatus));
        sendEmail(order.getUserEmail(),
                "[" + STORE_NAME + "] Cập nhật đơn hàng #" + order.getId(),
                templateEngine.process("email/order-status-update", ctx));
    }

    private Context buildOrderContext(OrderResponse order) {
        Context ctx = new Context();
        ctx.setVariable("storeName", STORE_NAME);
        ctx.setVariable("brandGreen", BRAND_GREEN);
        ctx.setVariable("orderId", order.getId());
        ctx.setVariable("userFullName", order.getUserFullName());
        ctx.setVariable("userEmail", order.getUserEmail());
        ctx.setVariable("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FMT) : "");
        ctx.setVariable("shippingAddress", order.getShippingAddress());
        ctx.setVariable("note", order.getNote());
        ctx.setVariable("statusLabel", statusLabel(order.getStatus()));
        ctx.setVariable("statusColor", statusColor(order.getStatus()));
        ctx.setVariable("items", buildFormattedItems(order.getItems()));
        ctx.setVariable("totalAmount", formatPrice(order.getTotalAmount()));
        return ctx;
    }

    private List<Map<String, Object>> buildFormattedItems(List<OrderItemResponse> items) {
        return items.stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productName", item.getProductName());
            m.put("quantity", item.getQuantity());
            m.put("unitPrice", formatPrice(item.getUnitPrice()));
            m.put("subtotal", formatPrice(item.getSubtotal()));
            return m;
        }).collect(Collectors.toList());
    }

    private void sendEmail(String toAddress, String subject, String htmlBody) {
        if (sesClient == null || props.getSes().getSenderEmail() == null
                || props.getSes().getSenderEmail().isBlank()) {
            log.warn("#NotificationServiceImpl.sendEmail - SES not available, skipping email to {}", toAddress);
            return;
        }
        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .fromEmailAddress(props.getSes().getSenderEmail())
                    .destination(Destination.builder().toAddresses(toAddress).build())
                    .content(EmailContent.builder()
                            .simple(Message.builder()
                                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                                    .body(Body.builder()
                                            .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                            .build())
                                    .build())
                            .build())
                    .build());
            log.info("#NotificationServiceImpl.sendEmail - sent to {}", toAddress);
        } catch (Exception e) {
            log.error("#NotificationServiceImpl.sendEmail - failed to send to {}: {}", toAddress, e.getMessage());
        }
    }

    private String statusLabel(OrderStatus status) {
        log.info("#NotificationServiceImpl.statusLabel - Status: {}", status);

        if (status == null) return "";
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case REJECTED -> "Đã từ chối";
        };
    }

    private String statusColor(OrderStatus status) {
        log.info("#NotificationServiceImpl.statusColor - Status: {}", status);

        if (status == null) return BRAND_GREEN;
        return switch (status) {
            case PENDING -> "#ea580c";
            case CONFIRMED -> "#1ba44c";
            case REJECTED -> "#b91c1c";
        };
    }

    private String formatPrice(BigDecimal amount) {
        log.info("#NotificationServiceImpl.formatPrice - Amount: {}", amount);

        if (amount == null) return "0đ";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.of("vi", "VN"));
        return nf.format(amount) + "đ";
    }
}
