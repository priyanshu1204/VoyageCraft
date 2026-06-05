package com.voyagecraft.dto.trip;

import com.voyagecraft.dto.destination.DestinationRequest;
import com.voyagecraft.enums.TripPace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TripRequest {

    @NotBlank(message = "Please provide a valid title")
    private String title;

    private String description;

    @NotNull(message = "Please provide a valid start date")
    private LocalDate startDate;

    @NotNull(message = "Please provide a valid end date")
    private LocalDate endDate;

    @Positive(message = "Budget must be positive")
    private BigDecimal budgetTotal;

    private String currency;

    private String coverImageUrl;

    private TripPace pace;

    @Valid
    private List<DestinationRequest> destinations;
}
