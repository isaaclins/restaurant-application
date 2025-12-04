package com.restaurant.product.repository;

import com.restaurant.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    
    Optional<Category> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    List<Category> findAllByOrderByDisplayOrderAsc();
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findActiveCategories();
}
