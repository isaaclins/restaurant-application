package com.restaurant.product.repository;

import com.restaurant.product.entity.Category;
import com.restaurant.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(Category category);
    
    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByAvailable(Boolean available);

    List<Product> findByCategoryAndAvailable(Category category, Boolean available);
    
    List<Product> findByCategoryIdAndAvailable(Long categoryId, Boolean available);

    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
}
