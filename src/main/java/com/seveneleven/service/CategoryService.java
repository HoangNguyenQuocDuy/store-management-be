package com.seveneleven.service;

import com.seveneleven.dto.category.CategoryRequest;
import com.seveneleven.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getById(Long id);

    CategoryResponse create(CategoryRequest request);

}
