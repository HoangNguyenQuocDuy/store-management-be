package com.seveneleven.controller;

import com.seveneleven.dto.ApiResponse;
import com.seveneleven.dto.category.CategoryResponse;
import com.seveneleven.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        log.info("#AuthController.list - START");

        return ApiResponse.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> detail(@PathVariable Long id) {
        log.info("#AuthController.detail - START - id: {}", id);

        return ApiResponse.ok(categoryService.getById(id));
    }
}
