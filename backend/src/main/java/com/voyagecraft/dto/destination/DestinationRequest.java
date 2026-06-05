package com.voyagecraft.dto.destination;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DestinationRequest {

    @NotBlank(message = "Please provide a valid destination name")
    private String destinationName;

    private String country;
    private Double latitude;
    private Double longitude;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private Integer orderIndex;
    private String notes;
}
