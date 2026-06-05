package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.stay.StayRequest;
import com.voyagecraft.dto.stay.StayResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.StayService;
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
@RequestMapping("/api/v1/stays")
@RequiredArgsConstructor
@Tag(name = "Stays", description = "Lodging management endpoints (US-04)")
public class StayController {

    private final StayService stayService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Add a stay to a trip")
    public ResponseEntity<ApiResponse<StayResponse>> addStay(
            @Valid @RequestBody StayRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        StayResponse response = stayService.addStay(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stay added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a stay")
    public ResponseEntity<ApiResponse<StayResponse>> updateStay(
            @PathVariable Long id,
            @Valid @RequestBody StayRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        StayResponse response = stayService.updateStay(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Stay updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stay")
    public ResponseEntity<ApiResponse<Void>> deleteStay(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        stayService.deleteStay(id, user);
        return ResponseEntity.ok(ApiResponse.success("Stay deleted", null));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get all stays for a trip (ordered by check-in date)")
    public ResponseEntity<ApiResponse<List<StayResponse>>> getTripStays(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<StayResponse> list = stayService.getTripStays(tripId, user);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single stay")
    public ResponseEntity<ApiResponse<StayResponse>> getStay(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        StayResponse response = stayService.getStay(id, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
