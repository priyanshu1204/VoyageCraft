package com.voyagecraft.controller;

import com.voyagecraft.dto.analytics.TripAnalyticsResponse;
import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AnalyticsService;
import com.voyagecraft.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Trip analytics and insights (US-14)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get full trip analytics")
    public ResponseEntity<ApiResponse<TripAnalyticsResponse>> getTripAnalytics(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTripAnalytics(tripId, user)));
    }
}
