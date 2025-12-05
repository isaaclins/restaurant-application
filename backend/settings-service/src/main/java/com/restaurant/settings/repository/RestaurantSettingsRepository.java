package com.restaurant.settings.repository;

import com.restaurant.settings.entity.RestaurantSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Restaurant Settings Repository
 */
@Repository
public interface RestaurantSettingsRepository extends JpaRepository<RestaurantSettings, Long> {

    Optional<RestaurantSettings> findFirstByOrderByIdAsc();
}
