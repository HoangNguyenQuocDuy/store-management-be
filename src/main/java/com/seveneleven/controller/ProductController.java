package com.seveneleven.controller;

import com.seveneleven.dto.ApiResponse;
import com.seveneleven.dto.PageResponse;
import com.seveneleven.dto.product.ProductRequest;
import com.seveneleven.dto.product.ProductResponse;
import com.seveneleven.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.info("#ProductController.list - START - search: {}, categoryId: {}", search, categoryId);

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        log.info("#ProductController.list - search: {}, categoryId: {}", search, categoryId);

        return ApiResponse.ok(productService.getAllProducts(search, categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> detail(@PathVariable Long id) {
        log.info("#ProductController.detail - START - id: {}", id);

        return ApiResponse.ok(productService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        log.info("#ProductController.create - START - name: {}", request.getName());

        ApiResponse<ProductResponse> response = ApiResponse.ok("Product created successfully", productService.create(request));
        log.info("#ProductController.create - created product #{}", response.getData().getId());
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        log.info("#ProductController.update - START - id: {}, name: {}", id, request.getName());

        ApiResponse<ProductResponse> response = ApiResponse.ok("Product updated successfully", productService.update(id, request));
        log.info("#ProductController.update - updated product #{}", id);

        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("#ProductController.delete - START - id: {}", id);

        productService.delete(id);
        return ApiResponse.ok("Deleted successfully", null);
    }
}
