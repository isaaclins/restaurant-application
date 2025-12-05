package com.restaurant.settings.repository;

import com.restaurant.settings.entity.OpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Opening Hours Repository
 */
@Repository
public interface OpeningHoursRepository extends JpaRepository<OpeningHours, Long> {

    Optional<OpeningHours> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<OpeningHours> findAllByOrderByDayOfWeekAsc();
}
