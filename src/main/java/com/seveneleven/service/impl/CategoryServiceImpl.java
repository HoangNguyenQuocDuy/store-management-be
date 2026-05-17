package com.seveneleven.service.impl;

import com.seveneleven.dto.category.CategoryRequest;
import com.seveneleven.dto.category.CategoryResponse;
import com.seveneleven.entity.Category;
import com.seveneleven.exception.ResourceNotFoundException;
import com.seveneleven.repository.CategoryRepository;
import com.seveneleven.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("#CategoryServiceImpl.getAllCategories - START");

        List<CategoryResponse> result = categoryRepository.findAll(Sort.by("name").ascending()).stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
        log.info("#CategoryServiceImpl.getAllCategories - found {} categories", result.size());

        return result;
    }

    @Override
    public CategoryResponse getById(Long id) {
        log.info("#CategoryServiceImpl.getById - START - id: {}", id);

        CategoryResponse response = CategoryResponse.from(
                categoryRepository.findById(id).orElseThrow(
                        () -> new ResourceNotFoundException("Category not found")
                )
        );
        log.info("#CategoryServiceImpl.getById - response: {}", response.getName());

        return response;
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        log.info("#CategoryServiceImpl.create - START - name: {}", request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        CategoryResponse response = CategoryResponse.from(categoryRepository.save(category));
        log.info("#CategoryServiceImpl.create - created category #{}: {}", response.getId(), response.getName());

        return response;
    }

}
