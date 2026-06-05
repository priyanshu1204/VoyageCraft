package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.packing.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.ClimateType;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.PackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packing")
@RequiredArgsConstructor
public class PackingController {

    private final PackingService packingService;
    private final AuthService authService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<PackingItemResponse>> addItem(
            @Valid @RequestBody PackingItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.addItem(request, user)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<PackingItemResponse>> updateItem(
            @PathVariable Long id, @Valid @RequestBody PackingItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.updateItem(id, request, user)));
    }

    @PatchMapping("/items/{id}/toggle")
    public ResponseEntity<ApiResponse<PackingItemResponse>> togglePacked(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.togglePacked(id, user)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        packingService.deleteItem(id, user);
        return ResponseEntity.ok(ApiResponse.success("Item deleted", null));
    }

    @GetMapping("/items/trip/{tripId}")
    public ResponseEntity<ApiResponse<List<PackingItemResponse>>> getTripItems(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.getTripItems(tripId, user)));
    }

    @GetMapping("/summary/{tripId}")
    public ResponseEntity<ApiResponse<PackingSummaryResponse>> getSummary(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.getSummary(tripId, user)));
    }

    @PostMapping("/template/{tripId}")
    public ResponseEntity<ApiResponse<List<PackingItemResponse>>> applyTemplate(
            @PathVariable Long tripId, @RequestParam ClimateType climate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Template applied", packingService.applyTemplate(tripId, climate, user)));
    }

    @GetMapping("/template/preview")
    public ResponseEntity<ApiResponse<List<PackingTemplateItem>>> previewTemplate(
            @RequestParam ClimateType climate) {
        return ResponseEntity.ok(ApiResponse.success(packingService.getTemplateItems(climate)));
    }

    @GetMapping("/climates")
    public ResponseEntity<ApiResponse<List<String>>> getClimates() {
        return ResponseEntity.ok(ApiResponse.success(packingService.getAvailableClimates()));
    }

    // ── User Documents ──────────────────────────────────────────────────

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<TravelDocumentResponse>> addDocument(
            @Valid @RequestBody TravelDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.addDocument(request, user)));
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<TravelDocumentResponse>> updateDocument(
            @PathVariable Long id, @Valid @RequestBody TravelDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.updateDocument(id, request, user)));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        packingService.deleteDocument(id, user);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    @GetMapping("/documents/trip/{tripId}")
    public ResponseEntity<ApiResponse<List<TravelDocumentResponse>>> getTripDocuments(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.getTripDocuments(tripId, user)));
    }

    @GetMapping("/documents/expiring/trip/{tripId}")
    public ResponseEntity<ApiResponse<List<TravelDocumentResponse>>> getExpiringByTrip(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.getExpiringByTrip(tripId, user)));
    }

    @GetMapping("/documents/expiring/me")
    public ResponseEntity<ApiResponse<List<TravelDocumentResponse>>> getMyExpiringDocuments(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(packingService.getMyExpiringDocuments(user)));
    }
}
