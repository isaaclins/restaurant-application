package com.restaurant.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.product.dto.AvailabilityRequest;
import com.restaurant.product.dto.ProductRequest;
import com.restaurant.product.entity.Category;
import com.restaurant.product.entity.Product;
import com.restaurant.product.repository.CategoryRepository;
import com.restaurant.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Product Controller Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create a test category for products
        testCategory = new Category();
        testCategory.setName("Pizza");
        testCategory.setDescription("Delicious pizzas");
        testCategory.setDisplayOrder(0);
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);
    }

    private Product createTestProduct(String name, BigDecimal price, boolean available) {
        Product product = Product.builder()
                .name(name)
                .description("Test description for " + name)
                .price(price)
                .category(testCategory)
                .available(available)
                .imageUrl("https://example.com/images/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .build();
        return productRepository.save(product);
    }

    @Nested
    @DisplayName("GET /api/products")
    class GetAllProducts {

        @Test
        @DisplayName("should return empty list when no products exist")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return all products")
        void shouldReturnAllProducts() throws Exception {
            createTestProduct("Margherita", new BigDecimal("12.50"), true);
            createTestProduct("Pepperoni", new BigDecimal("14.00"), true);
            createTestProduct("Hawaiian", new BigDecimal("15.00"), false);

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("should filter products by categoryId")
        void shouldFilterByCategoryId() throws Exception {
            createTestProduct("Margherita", new BigDecimal("12.50"), true);

            // Create another category with product
            Category pastaCategory = new Category();
            pastaCategory.setName("Pasta");
            pastaCategory.setDisplayOrder(1);
            pastaCategory.setIsActive(true);
            pastaCategory = categoryRepository.save(pastaCategory);

            Product pastaProduct = Product.builder()
                    .name("Spaghetti")
                    .price(new BigDecimal("10.00"))
                    .category(pastaCategory)
                    .available(true)
                    .build();
            productRepository.save(pastaProduct);

            mockMvc.perform(get("/api/products")
                    .param("categoryId", testCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Margherita")));
        }

        @Test
        @DisplayName("should filter products by availability")
        void shouldFilterByAvailability() throws Exception {
            createTestProduct("Margherita", new BigDecimal("12.50"), true);
            createTestProduct("Pepperoni", new BigDecimal("14.00"), false);

            mockMvc.perform(get("/api/products")
                    .param("available", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Margherita")));
        }

        @Test
        @DisplayName("should combine categoryId and availability filters")
        void shouldCombineFilters() throws Exception {
            createTestProduct("Margherita", new BigDecimal("12.50"), true);
            createTestProduct("Pepperoni", new BigDecimal("14.00"), false);

            mockMvc.perform(get("/api/products")
                    .param("categoryId", testCategory.getId().toString())
                    .param("available", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Pepperoni")));
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("should return product when exists")
        void shouldReturnProductWhenExists() throws Exception {
            Product product = createTestProduct("Margherita", new BigDecimal("12.50"), true);

            mockMvc.perform(get("/api/products/{id}", product.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Margherita")))
                    .andExpect(jsonPath("$.price", is(12.50)))
                    .andExpect(jsonPath("$.available", is(true)))
                    .andExpect(jsonPath("$.categoryName", is("Pizza")));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/products/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/products")
    class CreateProduct {

        @Test
        @DisplayName("should create product successfully")
        void shouldCreateProduct() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("New Pizza")
                    .description("A delicious new pizza")
                    .price(new BigDecimal("15.99"))
                    .categoryId(testCategory.getId())
                    .available(true)
                    .imageUrl("https://example.com/new-pizza.jpg")
                    .build();

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("New Pizza")))
                    .andExpect(jsonPath("$.description", is("A delicious new pizza")))
                    .andExpect(jsonPath("$.price", is(15.99)))
                    .andExpect(jsonPath("$.categoryName", is("Pizza")))
                    .andExpect(jsonPath("$.available", is(true)));

            assertThat(productRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("")
                    .price(new BigDecimal("15.99"))
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when price is missing")
        void shouldReturn400WhenPriceMissing() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("New Pizza")
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when price is negative")
        void shouldReturn400WhenPriceNegative() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("New Pizza")
                    .price(new BigDecimal("-5.00"))
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when categoryId is missing")
        void shouldReturn400WhenCategoryIdMissing() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("New Pizza")
                    .price(new BigDecimal("15.99"))
                    .build();

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 when category not found")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("New Pizza")
                    .price(new BigDecimal("15.99"))
                    .categoryId(999L)
                    .build();

            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("should update product successfully")
        void shouldUpdateProduct() throws Exception {
            Product product = createTestProduct("Old Name", new BigDecimal("10.00"), true);

            ProductRequest request = ProductRequest.builder()
                    .name("Updated Name")
                    .description("Updated description")
                    .price(new BigDecimal("19.99"))
                    .categoryId(testCategory.getId())
                    .available(false)
                    .build();

            mockMvc.perform(put("/api/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Updated Name")))
                    .andExpect(jsonPath("$.description", is("Updated description")))
                    .andExpect(jsonPath("$.price", is(19.99)))
                    .andExpect(jsonPath("$.available", is(false)));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenNotFound() throws Exception {
            ProductRequest request = ProductRequest.builder()
                    .name("Updated")
                    .price(new BigDecimal("10.00"))
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(put("/api/products/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should update product category")
        void shouldUpdateProductCategory() throws Exception {
            Product product = createTestProduct("Margherita", new BigDecimal("12.50"), true);

            Category newCategory = new Category();
            newCategory.setName("Specials");
            newCategory.setDisplayOrder(5);
            newCategory.setIsActive(true);
            newCategory = categoryRepository.save(newCategory);

            ProductRequest request = ProductRequest.builder()
                    .name("Margherita")
                    .price(new BigDecimal("12.50"))
                    .categoryId(newCategory.getId())
                    .build();

            mockMvc.perform(put("/api/products/{id}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categoryName", is("Specials")));
        }
    }

    @Nested
    @DisplayName("PUT /api/products/{id}/availability")
    class UpdateAvailability {

        @Test
        @DisplayName("should update availability to false")
        void shouldUpdateAvailabilityToFalse() throws Exception {
            Product product = createTestProduct("Margherita", new BigDecimal("12.50"), true);

            AvailabilityRequest request = new AvailabilityRequest();
            request.setAvailable(false);

            mockMvc.perform(put("/api/products/{id}/availability", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available", is(false)));
        }

        @Test
        @DisplayName("should update availability to true")
        void shouldUpdateAvailabilityToTrue() throws Exception {
            Product product = createTestProduct("Margherita", new BigDecimal("12.50"), false);

            AvailabilityRequest request = new AvailabilityRequest();
            request.setAvailable(true);

            mockMvc.perform(put("/api/products/{id}/availability", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available", is(true)));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenNotFound() throws Exception {
            AvailabilityRequest request = new AvailabilityRequest();
            request.setAvailable(false);

            mockMvc.perform(put("/api/products/{id}/availability", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("should delete product successfully")
        void shouldDeleteProduct() throws Exception {
            Product product = createTestProduct("ToDelete", new BigDecimal("10.00"), true);

            mockMvc.perform(delete("/api/products/{id}", product.getId()))
                    .andExpect(status().isNoContent());

            assertThat(productRepository.findById(product.getId())).isEmpty();
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/products/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }
}
