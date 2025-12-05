package com.restaurant.settings.repository;

import com.restaurant.settings.entity.SpecialHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Special Hours Repository
 */
@Repository
public interface SpecialHoursRepository extends JpaRepository<SpecialHours, Long> {

    Optional<SpecialHours> findByDate(LocalDate date);

    List<SpecialHours> findByDateAfterOrderByDateAsc(LocalDate date);

    List<SpecialHours> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);
}
