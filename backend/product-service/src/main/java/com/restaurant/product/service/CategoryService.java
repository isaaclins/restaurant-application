package com.restaurant.product.service;

import com.restaurant.product.dto.CategoryRequest;
import com.restaurant.product.dto.CategoryResponse;
import com.restaurant.product.entity.Category;
import com.restaurant.product.exception.ResourceNotFoundException;
import com.restaurant.product.repository.CategoryRepository;
import com.restaurant.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToResponseWithCount)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findActiveCategories()
                .stream()
                .map(this::mapToResponseWithCount)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponseWithCount(category);
    }

    public CategoryResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return mapToResponseWithCount(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = new Category();
        mapRequestToEntity(request, category);
        
        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if name is being changed and new name already exists
        if (!category.getName().equalsIgnoreCase(request.getName()) 
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        mapRequestToEntity(request, category);
        Category saved = categoryRepository.save(category);
        return mapToResponseWithCount(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        // Check if category has products
        Long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with " + productCount + " products. Move or delete products first.");
        }
        
        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse toggleCategoryActive(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        category.setIsActive(!category.getIsActive());
        Category saved = categoryRepository.save(category);
        return mapToResponseWithCount(saved);
    }

    @Transactional
    public List<CategoryResponse> reorderCategories(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Long id = categoryIds.get(i);
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
            category.setDisplayOrder(i);
            categoryRepository.save(category);
        }
        return getAllCategories();
    }

    private void mapRequestToEntity(CategoryRequest request, Category category) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        category.setIconUrl(request.getIconUrl());
        category.setColorCode(request.getColorCode());
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder(),
                category.getIsActive(),
                category.getIconUrl(),
                category.getColorCode(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private CategoryResponse mapToResponseWithCount(Category category) {
        CategoryResponse response = mapToResponse(category);
        response.setProductCount(productRepository.countByCategoryId(category.getId()));
        return response;
    }
}
