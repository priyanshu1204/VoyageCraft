package com.voyagecraft.dto.transport;

import com.voyagecraft.enums.TransportType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransportResponse {
    private Long id;
    private Long tripId;
    private TransportType type;
    private String provider;
    private String flightNumber;
    private String departureLocation;
    private String arrivalLocation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String departureTimezone;
    private String arrivalTimezone;
    private String bookingReference;
    private BigDecimal cost;
    private String notes;
    private Long durationMinutes;
    private String departureTimeFormatted; // timezone-adjusted display string
    private String arrivalTimeFormatted;
    private LocalDateTime createdAt;
}
