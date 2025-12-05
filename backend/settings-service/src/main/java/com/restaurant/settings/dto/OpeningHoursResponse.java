package com.restaurant.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Opening Hours Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHoursResponse {
    private Long id;
    private DayOfWeek dayOfWeek;
    private String dayName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private Boolean isClosed;
    private LocalTime deliveryStartTime;
    private LocalTime deliveryEndTime;
}
