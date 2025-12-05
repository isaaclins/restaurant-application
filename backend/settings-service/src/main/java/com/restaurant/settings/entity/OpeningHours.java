package com.restaurant.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Opening Hours Entity - Restaurant operating hours per day
 */
@Entity
@Table(name = "opening_hours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    // For split hours (e.g., lunch and dinner service)
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    @Builder.Default
    private Boolean isClosed = false;

    // Different hours for delivery
    private LocalTime deliveryStartTime;
    private LocalTime deliveryEndTime;
}
