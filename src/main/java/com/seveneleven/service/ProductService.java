package com.seveneleven.service;

import com.seveneleven.dto.PageResponse;
import com.seveneleven.dto.product.ProductRequest;
import com.seveneleven.dto.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    PageResponse<ProductResponse> getAllProducts(String search, Long categoryId, Pageable pageable);

    ProductResponse getById(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

}
