package com.restaurant.settings.repository;

import com.restaurant.settings.entity.DeliveryArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Delivery Area Repository
 */
@Repository
public interface DeliveryAreaRepository extends JpaRepository<DeliveryArea, Long> {

    Optional<DeliveryArea> findByPostalCode(String postalCode);

    List<DeliveryArea> findByIsActiveTrue();

    boolean existsByPostalCode(String postalCode);
}
