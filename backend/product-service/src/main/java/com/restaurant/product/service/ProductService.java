package com.restaurant.product.service;

import com.restaurant.product.dto.AvailabilityRequest;
import com.restaurant.product.dto.ProductRequest;
import com.restaurant.product.dto.ProductResponse;
import com.restaurant.product.entity.Category;
import com.restaurant.product.entity.Product;
import com.restaurant.product.exception.ProductNotFoundException;
import com.restaurant.product.exception.ResourceNotFoundException;
import com.restaurant.product.exception.DuplicateProductException;
import com.restaurant.product.repository.CategoryRepository;
import com.restaurant.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service - Business Logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Get all products with optional filters
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts(Long categoryId, Boolean available) {
        List<Product> products;

        if (categoryId != null && available != null) {
            products = productRepository.findByCategoryIdAndAvailable(categoryId, available);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else if (available != null) {
            products = productRepository.findByAvailable(available);
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return ProductResponse.fromEntity(product);
    }

    /**
     * Create new product
     */
    public ProductResponse createProduct(ProductRequest request) {
        // Check for duplicate name
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateProductException("Product with name '" + request.getName() + "' already exists");
        }

        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .imageUrl(request.getImageUrl())
                .preparationTime(request.getPreparationTime())
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product: {}", saved.getId());

        return ProductResponse.fromEntity(saved);
    }

    /**
     * Update existing product
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        // Update fields
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getAvailable() != null) {
            product.setAvailable(request.getAvailable());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getPreparationTime() != null) {
            product.setPreparationTime(request.getPreparationTime());
        }

        Product saved = productRepository.save(product);
        log.info("Updated product: {}", saved.getId());

        return ProductResponse.fromEntity(saved);
    }

    /**
     * Update product availability
     */
    public ProductResponse updateAvailability(Long id, AvailabilityRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setAvailable(request.getAvailable());
        Product saved = productRepository.save(product);

        log.info("Updated availability for product {}: {}", id, request.getAvailable());

        return ProductResponse.fromEntity(saved);
    }

    /**
     * Delete product
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Deleted product: {}", id);
    }
}
