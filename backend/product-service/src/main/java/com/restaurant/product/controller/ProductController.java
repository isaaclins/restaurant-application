package com.restaurant.product.controller;

import com.restaurant.product.dto.AvailabilityRequest;
import com.restaurant.product.dto.ProductRequest;
import com.restaurant.product.dto.ProductResponse;
import com.restaurant.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product Controller - REST API Endpoints
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management API")
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products - Get all products
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with optional filters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by availability")
            @RequestParam(required = false) Boolean available) {
        
        List<ProductResponse> products = productService.getAllProducts(categoryId, available);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id} - Get product by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {
        
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * POST /api/products - Create new product
     */
    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "409", description = "Product with same name exists")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * PUT /api/products/{id} - Update product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    /**
     * PUT /api/products/{id}/availability - Update availability
     */
    @PutMapping("/{id}/availability")
    @Operation(summary = "Update availability", description = "Toggle product availability (Staff)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Availability updated"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateAvailability(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        
        ProductResponse product = productService.updateAvailability(id, request);
        return ResponseEntity.ok(product);
    }

    /**
     * DELETE /api/products/{id} - Delete product
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product (Admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
