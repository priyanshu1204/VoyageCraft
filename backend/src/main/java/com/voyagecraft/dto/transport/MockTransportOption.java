package com.voyagecraft.dto.transport;

import com.voyagecraft.enums.TransportType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MockTransportOption {
    private TransportType type;
    private String provider;
    private String flightNumber;
    private String departureLocation;
    private String arrivalLocation;
    private String departureTime;
    private String arrivalTime;
    private String departureTimezone;
    private String arrivalTimezone;
    private Long durationMinutes;
    private BigDecimal cost;
    private int stops;
}
