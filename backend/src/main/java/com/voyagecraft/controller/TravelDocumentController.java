package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.document.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.TravelDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Travel Documents", description = "Visa & Entry Guidelines (US-11)")
public class TravelDocumentController {

    private final TravelDocumentService documentService;
    private final AuthService authService;

    @PostMapping("/trip/{tripId}")
    @Operation(summary = "Create a travel document for a country in a trip")
    public ResponseEntity<ApiResponse<TravelDocumentResponse>> create(
            @PathVariable Long tripId, @RequestBody TravelDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Document created", documentService.createDocument(tripId, request, user)));
    }

    @PutMapping("/{docId}")
    @Operation(summary = "Update a travel document")
    public ResponseEntity<ApiResponse<TravelDocumentResponse>> update(
            @PathVariable Long docId, @RequestBody TravelDocumentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Document updated", documentService.updateDocument(docId, request, user)));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get all travel documents for a trip")
    public ResponseEntity<ApiResponse<List<TravelDocumentResponse>>> getTripDocs(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(documentService.getTripDocuments(tripId, user)));
    }

    @GetMapping("/{docId}")
    @Operation(summary = "Get a single travel document")
    public ResponseEntity<ApiResponse<TravelDocumentResponse>> getDoc(
            @PathVariable Long docId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocument(docId, user)));
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "Delete a travel document")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long docId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        documentService.deleteDocument(docId, user);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    @PostMapping("/{docId}/checklist")
    @Operation(summary = "Add a checklist item to a document")
    public ResponseEntity<ApiResponse<ChecklistItemResponse>> addChecklist(
            @PathVariable Long docId, @RequestBody ChecklistItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Item added", documentService.addChecklistItemToDoc(docId, request, user)));
    }

    @PatchMapping("/checklist/{itemId}/toggle")
    @Operation(summary = "Toggle checklist item completion")
    public ResponseEntity<ApiResponse<ChecklistItemResponse>> toggleChecklist(
            @PathVariable Long itemId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Toggled", documentService.toggleChecklistItem(itemId, user)));
    }

    @DeleteMapping("/checklist/{itemId}")
    @Operation(summary = "Delete a checklist item")
    public ResponseEntity<ApiResponse<Void>> deleteChecklist(
            @PathVariable Long itemId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        documentService.deleteChecklistItem(itemId, user);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    @PostMapping("/{docId}/reminders")
    @Operation(summary = "Add a reminder to a document")
    public ResponseEntity<ApiResponse<ReminderResponse>> addReminder(
            @PathVariable Long docId, @RequestBody ReminderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reminder added", documentService.addReminderToDoc(docId, request, user)));
    }

    @PatchMapping("/reminders/{reminderId}/dismiss")
    @Operation(summary = "Dismiss a reminder")
    public ResponseEntity<ApiResponse<ReminderResponse>> dismissReminder(
            @PathVariable Long reminderId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Dismissed", documentService.dismissReminder(reminderId, user)));
    }

    @DeleteMapping("/reminders/{reminderId}")
    @Operation(summary = "Delete a reminder")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(
            @PathVariable Long reminderId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        documentService.deleteReminder(reminderId, user);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    @GetMapping("/reminders/upcoming")
    @Operation(summary = "Get upcoming reminders for the user")
    public ResponseEntity<ApiResponse<List<ReminderResponse>>> getUpcoming(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(documentService.getUpcomingReminders(user)));
    }

    @GetMapping("/library")
    @Operation(summary = "Get static country visa/entry library")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLibrary() {
        return ResponseEntity.ok(ApiResponse.success(documentService.getCountryLibrary()));
    }
}
