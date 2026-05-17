package com.seveneleven.repository.spec;

import com.seveneleven.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpec {

    public static Specification<Product> hasSearch(String search) {
        if (search == null || search.isBlank()) return Specification.where(null);
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        if (categoryId == null) return Specification.where(null);
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }
}
