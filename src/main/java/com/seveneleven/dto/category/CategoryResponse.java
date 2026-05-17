package com.seveneleven.dto.category;

import com.seveneleven.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class CategoryResponse {

    private Long id;

    private String name;

    private String description;

    private OffsetDateTime createdAt;

    public static CategoryResponse from(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
