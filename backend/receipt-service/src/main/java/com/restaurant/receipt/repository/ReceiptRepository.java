package com.restaurant.receipt.repository;

import com.restaurant.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Receipt Repository
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    Optional<Receipt> findByOrderId(Long orderId);

    Optional<Receipt> findByOrderNumber(String orderNumber);

    List<Receipt> findByCustomerId(Long customerId);

    List<Receipt> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Receipt r WHERE DATE(r.createdAt) = DATE(:date)")
    List<Receipt> findByDate(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(r) FROM Receipt r WHERE DATE(r.createdAt) = DATE(:date)")
    long countByDate(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(r.totalAmount) FROM Receipt r WHERE DATE(r.createdAt) = DATE(:date)")
    java.math.BigDecimal sumTotalByDate(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(r.vatAmount) FROM Receipt r WHERE DATE(r.createdAt) = DATE(:date)")
    java.math.BigDecimal sumVatByDate(@Param("date") LocalDateTime date);

    boolean existsByOrderId(Long orderId);
}
