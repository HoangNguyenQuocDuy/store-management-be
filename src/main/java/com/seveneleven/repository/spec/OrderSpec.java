package com.seveneleven.repository.spec;

import com.seveneleven.entity.Order;
import com.seveneleven.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

public class OrderSpec {

    public static Specification<Order> hasStatus(OrderStatus status) {
        if (status == null) return Specification.where(null);
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Order> fromDate(OffsetDateTime from) {
        if (from == null) return Specification.where(null);
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Order> toDate(OffsetDateTime to) {
        if (to == null) return Specification.where(null);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Order> hasUserEmail(String email) {
        if (email == null || email.isBlank()) return Specification.where(null);
        String pattern = "%" + email.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("user").get("email")), pattern);
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

}