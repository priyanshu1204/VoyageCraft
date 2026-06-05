package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.destination.DestinationRequest;
import com.voyagecraft.dto.destination.DestinationResponse;
import com.voyagecraft.service.TripDestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/destinations")
@RequiredArgsConstructor
@Tag(name = "Destinations", description = "Trip destination endpoints")
public class TripDestinationController {

    private final TripDestinationService destinationService;

    @PostMapping
    @Operation(summary = "Add destination to trip")
    public ResponseEntity<ApiResponse<DestinationResponse>> addDestination(
            @PathVariable Long tripId,
            @Valid @RequestBody DestinationRequest request) {
        DestinationResponse response = destinationService.addDestination(tripId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Destination added", response));
    }

    @GetMapping
    @Operation(summary = "Get all destinations for trip")
    public ResponseEntity<ApiResponse<List<DestinationResponse>>> getDestinations(@PathVariable Long tripId) {
        List<DestinationResponse> destinations = destinationService.getDestinations(tripId);
        return ResponseEntity.ok(ApiResponse.success(destinations));
    }

    @PutMapping("/{destId}")
    @Operation(summary = "Update destination")
    public ResponseEntity<ApiResponse<DestinationResponse>> updateDestination(
            @PathVariable Long tripId,
            @PathVariable Long destId,
            @Valid @RequestBody DestinationRequest request) {
        DestinationResponse response = destinationService.updateDestination(tripId, destId, request);
        return ResponseEntity.ok(ApiResponse.success("Destination updated", response));
    }

    @DeleteMapping("/{destId}")
    @Operation(summary = "Remove destination from trip")
    public ResponseEntity<ApiResponse<Void>> deleteDestination(
            @PathVariable Long tripId,
            @PathVariable Long destId) {
        destinationService.deleteDestination(tripId, destId);
        return ResponseEntity.ok(ApiResponse.success("Destination removed", null));
    }
}
