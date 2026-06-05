package com.voyagecraft.dto.transport;

import com.voyagecraft.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransportRequest {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Transport type is required")
    private TransportType type;

    private String provider;
    private String flightNumber;

    @NotBlank(message = "Departure location is required")
    private String departureLocation;

    @NotBlank(message = "Arrival location is required")
    private String arrivalLocation;

    @NotBlank(message = "Departure time is required")
    private String departureTime; // ISO-8601: "2026-06-01T10:30:00"

    @NotBlank(message = "Arrival time is required")
    private String arrivalTime;

    private String departureTimezone; // e.g. "Asia/Kolkata"
    private String arrivalTimezone;

    private String bookingReference;
    private BigDecimal cost;
    private String notes;
}
