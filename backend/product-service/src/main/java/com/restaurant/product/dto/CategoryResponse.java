package com.restaurant.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private String iconUrl;
    private String colorCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long productCount;

    public CategoryResponse(Long id, String name, String description, Integer displayOrder, 
                           Boolean isActive, String iconUrl, String colorCode,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
        this.iconUrl = iconUrl;
        this.colorCode = colorCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
