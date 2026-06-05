package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.template.TemplateResponse;
import com.voyagecraft.dto.trip.TripResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.TripTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Templates", description = "Trip template endpoints")
public class TripTemplateController {

    private final TripTemplateService templateService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get all trip templates")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getTemplates() {
        List<TemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable Long id) {
        TemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @PostMapping("/{id}/create-trip")
    @Operation(summary = "Create trip from template")
    public ResponseEntity<ApiResponse<TripResponse>> createFromTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        LocalDate startDate = LocalDate.parse(body.getOrDefault("startDate", LocalDate.now().plusDays(7).toString()));
        TripResponse response = templateService.createTripFromTemplate(id, startDate, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Trip created from template", response));
    }
}
