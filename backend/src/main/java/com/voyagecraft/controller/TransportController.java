package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.transport.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.TransportType;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transports")
@RequiredArgsConstructor
@Tag(name = "Transport", description = "Transportation segment management endpoints (US-03)")
public class TransportController {

    private final TransportService transportService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Add a transport segment to a trip")
    public ResponseEntity<ApiResponse<TransportResponse>> addTransport(
            @Valid @RequestBody TransportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TransportResponse response = transportService.addTransport(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transport segment added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transport segment")
    public ResponseEntity<ApiResponse<TransportResponse>> updateTransport(
            @PathVariable Long id,
            @Valid @RequestBody TransportRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TransportResponse response = transportService.updateTransport(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Transport segment updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transport segment")
    public ResponseEntity<ApiResponse<Void>> deleteTransport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        transportService.deleteTransport(id, user);
        return ResponseEntity.ok(ApiResponse.success("Transport segment deleted", null));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get all transport segments for a trip (ordered by departure time)")
    public ResponseEntity<ApiResponse<List<TransportResponse>>> getTripTransports(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<TransportResponse> list = transportService.getTripTransports(tripId, user);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single transport segment")
    public ResponseEntity<ApiResponse<TransportResponse>> getTransport(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        TransportResponse response = transportService.getTransport(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/trip/{tripId}/conflicts")
    @Operation(summary = "Detect scheduling conflicts and tight connections",
               description = "Analyzes all segments for overlaps, impossible connections, and layovers shorter than minimum recommended times. Uses timezone-aware comparisons.")
    public ResponseEntity<ApiResponse<List<TransportConflictResponse>>> detectConflicts(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<TransportConflictResponse> conflicts = transportService.detectConflicts(tripId, user);
        return ResponseEntity.ok(ApiResponse.success(conflicts));
    }

    @GetMapping("/search")
    @Operation(summary = "Search mock transport options (flights/trains/buses)",
               description = "Returns mock transport schedules between two locations for a given date. This simulates a real booking API integration.")
    public ResponseEntity<ApiResponse<List<MockTransportOption>>> searchMockTransport(
            @RequestParam TransportType type,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String date,
            @RequestParam(required = false) String fromTimezone,
            @RequestParam(required = false) String toTimezone) {
        List<MockTransportOption> options = transportService.searchMockTransport(type, from, to, date, fromTimezone, toTimezone);
        return ResponseEntity.ok(ApiResponse.success(options));
    }
}
