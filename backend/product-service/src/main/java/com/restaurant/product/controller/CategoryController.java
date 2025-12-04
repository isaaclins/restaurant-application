package com.restaurant.product.controller;

import com.restaurant.product.dto.CategoryRequest;
import com.restaurant.product.dto.CategoryResponse;
import com.restaurant.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management API")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns all categories ordered by display order")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Returns only active categories for customer-facing menus")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name")
    public ResponseEntity<CategoryResponse> getCategoryByName(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }

    @PostMapping
    @Operation(summary = "Create new category", description = "Creates a new product category (Restaurant Client only)")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category (Restaurant Client only)")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category if it has no products (Restaurant Client only)")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle category active status", description = "Enables/disables category visibility")
    public ResponseEntity<CategoryResponse> toggleCategoryActive(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.toggleCategoryActive(id));
    }

    @PutMapping("/reorder")
    @Operation(summary = "Reorder categories", description = "Updates display order based on provided ID sequence")
    public ResponseEntity<List<CategoryResponse>> reorderCategories(@RequestBody List<Long> categoryIds) {
        return ResponseEntity.ok(categoryService.reorderCategories(categoryIds));
    }
}
