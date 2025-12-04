package com.restaurant.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Category Entity - Dynamically manageable product categories
 * 
 * Categories can be created, updated, and deleted via the Restaurant Client.
 * Each category has a unique name, optional description, display order, and active status.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Column(unique = true, nullable = false)
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    /**
     * Display order for sorting categories in menus
     * Lower numbers appear first
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * Active status - inactive categories are hidden from customers
     * but still visible in the restaurant client for management
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Icon or image URL for category display
     */
    @Size(max = 500)
    @Column(name = "icon_url")
    private String iconUrl;

    /**
     * Color code for UI display (hex format, e.g., #FF5733)
     */
    @Size(max = 7)
    @Column(name = "color_code")
    private String colorCode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
