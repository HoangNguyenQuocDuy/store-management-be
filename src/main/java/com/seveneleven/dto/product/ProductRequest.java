package com.seveneleven.dto.product;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private Long categoryId;

    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @Min(0)
    private Integer stockQuantity = 0;

    private String imageUrl;
}
