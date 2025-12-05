package com.restaurant.settings.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Special Hours Entity - Holiday or special event hours
 */
@Entity
@Table(name = "special_hours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private String name; // e.g., "Christmas", "New Year's Eve"

    @Builder.Default
    private Boolean isClosed = false;

    private LocalTime openTime;
    private LocalTime closeTime;

    private String note;
}
