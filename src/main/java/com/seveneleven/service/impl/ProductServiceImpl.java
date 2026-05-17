package com.seveneleven.service.impl;

import com.seveneleven.dto.PageResponse;
import com.seveneleven.dto.product.ProductRequest;
import com.seveneleven.dto.product.ProductResponse;
import com.seveneleven.entity.Category;
import com.seveneleven.entity.Product;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.repository.CategoryRepository;
import com.seveneleven.repository.ProductRepository;
import com.seveneleven.repository.spec.ProductSpec;
import com.seveneleven.service.ProductService;
import com.seveneleven.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final S3UploadService s3UploadService;

    @Override
    public PageResponse<ProductResponse> getAllProducts(String search, Long categoryId, Pageable pageable) {
        log.info("#ProductServiceImpl.getAllProducts - START - search: {}, categoryId: {}", search, categoryId);

        Specification<Product> spec = Specification.where(ProductSpec.hasSearch(search))
                .and(ProductSpec.hasCategory(categoryId));
        PageResponse<ProductResponse> result = PageResponse.of(productRepository.findAll(spec, pageable).map(ProductResponse::from));
        log.info("#ProductServiceImpl.getAllProducts - totalElements: {}", result.getTotalElements());

        return result;
    }

    @Override
    public ProductResponse getById(Long id) {
        log.info("#ProductServiceImpl.getById - START - id: {}", id);

        ProductResponse response = ProductResponse.from(findProduct(id));
        log.info("#ProductServiceImpl.getById - found: {}", response.getName());

        return response;
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("#ProductServiceImpl.create - START - name: {}, categoryId: {}, price: {}", request.getName(), request.getCategoryId(), request.getPrice());

        Category category = findCategory(request.getCategoryId());
        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();

        ProductResponse response = ProductResponse.from(productRepository.save(product));
        log.info("#ProductServiceImpl.create - created product #{}: {}", response.getId(), response.getName());

        return response;
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("#ProductServiceImpl.update - START - id: {}, name: {}", id, request.getName());

        Product product = findProduct(id);
        String oldImageUrl = product.getImageUrl();
        Category category = findCategory(request.getCategoryId());
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        if (oldImageUrl != null && !oldImageUrl.equals(request.getImageUrl())) {
            s3UploadService.deleteImage(oldImageUrl);
        }
        ProductResponse response = ProductResponse.from(productRepository.save(product));
        log.info("#ProductServiceImpl.update - updated product #{}", id);

        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("#ProductServiceImpl.delete - START - id: {}", id);

        Product product = findProduct(id);
        productRepository.deleteById(id);
        s3UploadService.deleteImage(product.getImageUrl());

        log.info("#ProductServiceImpl.delete - deleted product #{}", id);
    }

    private Product findProduct(Long id) {
        log.info("#ProductServiceImpl.findProduct - START - productId: {}", id);

        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private Category findCategory(Long id) {
        log.info("#ProductServiceImpl.findCategory - START - categoryId: {}", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

}
