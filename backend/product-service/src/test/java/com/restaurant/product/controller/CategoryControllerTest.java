package com.restaurant.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.product.dto.CategoryRequest;
import com.restaurant.product.dto.CategoryResponse;
import com.restaurant.product.entity.Category;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Category Controller Integration Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category createTestCategory(String name, int displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test description for " + name);
        category.setDisplayOrder(displayOrder);
        category.setIsActive(true);
        category.setColorCode("#FF5733");
        return categoryRepository.save(category);
    }

    @Nested
    @DisplayName("GET /api/categories")
    class GetAllCategories {

        @Test
        @DisplayName("should return empty list when no categories exist")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return all categories ordered by displayOrder")
        void shouldReturnAllCategoriesOrdered() throws Exception {
            createTestCategory("Desserts", 2);
            createTestCategory("Pizza", 0);
            createTestCategory("Pasta", 1);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].name", is("Pizza")))
                    .andExpect(jsonPath("$[1].name", is("Pasta")))
                    .andExpect(jsonPath("$[2].name", is("Desserts")));
        }
    }

    @Nested
    @DisplayName("GET /api/categories/active")
    class GetActiveCategories {

        @Test
        @DisplayName("should return only active categories")
        void shouldReturnOnlyActiveCategories() throws Exception {
            Category active = createTestCategory("Pizza", 0);
            Category inactive = createTestCategory("Seasonal", 1);
            inactive.setIsActive(false);
            categoryRepository.save(inactive);

            mockMvc.perform(get("/api/categories/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("Pizza")));
        }
    }

    @Nested
    @DisplayName("GET /api/categories/{id}")
    class GetCategoryById {

        @Test
        @DisplayName("should return category when exists")
        void shouldReturnCategoryWhenExists() throws Exception {
            Category category = createTestCategory("Pizza", 0);

            mockMvc.perform(get("/api/categories/{id}", category.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(category.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Pizza")))
                    .andExpect(jsonPath("$.colorCode", is("#FF5733")));
        }

        @Test
        @DisplayName("should return 404 when category not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/categories/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/categories/name/{name}")
    class GetCategoryByName {

        @Test
        @DisplayName("should return category by name (case insensitive)")
        void shouldReturnCategoryByName() throws Exception {
            createTestCategory("Pizza", 0);

            mockMvc.perform(get("/api/categories/name/{name}", "pizza"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("Pizza")));
        }

        @Test
        @DisplayName("should return 404 when name not found")
        void shouldReturn404WhenNameNotFound() throws Exception {
            mockMvc.perform(get("/api/categories/name/{name}", "NonExistent"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/categories")
    class CreateCategory {

        @Test
        @DisplayName("should create category successfully")
        void shouldCreateCategory() throws Exception {
            CategoryRequest request = new CategoryRequest();
            request.setName("New Category");
            request.setDescription("A new category");
            request.setDisplayOrder(5);
            request.setIsActive(true);
            request.setColorCode("#00FF00");

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is("New Category")))
                    .andExpect(jsonPath("$.description", is("A new category")))
                    .andExpect(jsonPath("$.displayOrder", is(5)))
                    .andExpect(jsonPath("$.colorCode", is("#00FF00")));

            assertThat(categoryRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameBlank() throws Exception {
            CategoryRequest request = new CategoryRequest();
            request.setName("");

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when category name already exists")
        void shouldReturn400WhenDuplicateName() throws Exception {
            createTestCategory("Pizza", 0);

            CategoryRequest request = new CategoryRequest();
            request.setName("Pizza");

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/categories/{id}")
    class UpdateCategory {

        @Test
        @DisplayName("should update category successfully")
        void shouldUpdateCategory() throws Exception {
            Category category = createTestCategory("Old Name", 0);

            CategoryRequest request = new CategoryRequest();
            request.setName("New Name");
            request.setDescription("Updated description");
            request.setDisplayOrder(10);

            mockMvc.perform(put("/api/categories/{id}", category.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is("New Name")))
                    .andExpect(jsonPath("$.description", is("Updated description")))
                    .andExpect(jsonPath("$.displayOrder", is(10)));
        }

        @Test
        @DisplayName("should return 404 when category not found")
        void shouldReturn404WhenNotFound() throws Exception {
            CategoryRequest request = new CategoryRequest();
            request.setName("Updated");

            mockMvc.perform(put("/api/categories/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/categories/{id}")
    class DeleteCategory {

        @Test
        @DisplayName("should delete category when no products assigned")
        void shouldDeleteEmptyCategory() throws Exception {
            Category category = createTestCategory("ToDelete", 0);

            mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                    .andExpect(status().isNoContent());

            assertThat(categoryRepository.findById(category.getId())).isEmpty();
        }

        @Test
        @DisplayName("should return 404 when category not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/categories/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/categories/{id}/toggle-active")
    class ToggleCategoryActive {

        @Test
        @DisplayName("should toggle category from active to inactive")
        void shouldToggleToInactive() throws Exception {
            Category category = createTestCategory("Pizza", 0);
            assertThat(category.getIsActive()).isTrue();

            mockMvc.perform(patch("/api/categories/{id}/toggle-active", category.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive", is(false)));
        }

        @Test
        @DisplayName("should toggle category from inactive to active")
        void shouldToggleToActive() throws Exception {
            Category category = createTestCategory("Pizza", 0);
            category.setIsActive(false);
            categoryRepository.save(category);

            mockMvc.perform(patch("/api/categories/{id}/toggle-active", category.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive", is(true)));
        }
    }

    @Nested
    @DisplayName("PUT /api/categories/reorder")
    class ReorderCategories {

        @Test
        @DisplayName("should reorder categories by provided IDs")
        void shouldReorderCategories() throws Exception {
            Category cat1 = createTestCategory("First", 0);
            Category cat2 = createTestCategory("Second", 1);
            Category cat3 = createTestCategory("Third", 2);

            // Reorder: Third, First, Second
            List<Long> newOrder = List.of(cat3.getId(), cat1.getId(), cat2.getId());

            mockMvc.perform(put("/api/categories/reorder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newOrder)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name", is("Third")))
                    .andExpect(jsonPath("$[0].displayOrder", is(0)))
                    .andExpect(jsonPath("$[1].name", is("First")))
                    .andExpect(jsonPath("$[1].displayOrder", is(1)))
                    .andExpect(jsonPath("$[2].name", is("Second")))
                    .andExpect(jsonPath("$[2].displayOrder", is(2)));
        }
    }
}
