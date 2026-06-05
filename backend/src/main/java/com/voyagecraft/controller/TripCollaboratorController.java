package com.voyagecraft.controller;

import com.voyagecraft.dto.collaborator.CollaboratorRequest;
import com.voyagecraft.dto.collaborator.CollaboratorResponse;
import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.TripCollaboratorService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/collaborators")
@RequiredArgsConstructor
@Tag(name = "Collaborators", description = "Trip collaboration endpoints")
public class TripCollaboratorController {

    private final TripCollaboratorService collaboratorService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Invite collaborator to trip")
    public ResponseEntity<ApiResponse<CollaboratorResponse>> invite(
            @PathVariable Long tripId,
            @Valid @RequestBody CollaboratorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        CollaboratorResponse response = collaboratorService.inviteCollaborator(tripId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Invitation sent", response));
    }

    @GetMapping
    @Operation(summary = "Get all collaborators for trip")
    public ResponseEntity<ApiResponse<List<CollaboratorResponse>>> getCollaborators(@PathVariable Long tripId) {
        List<CollaboratorResponse> collaborators = collaboratorService.getCollaborators(tripId);
        return ResponseEntity.ok(ApiResponse.success(collaborators));
    }

    @PutMapping("/{collabId}")
    @Operation(summary = "Respond to invitation or update role")
    public ResponseEntity<ApiResponse<CollaboratorResponse>> updateCollaborator(
            @PathVariable Long tripId,
            @PathVariable Long collabId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());

        if (body.containsKey("accept")) {
            boolean accept = Boolean.parseBoolean(body.get("accept"));
            CollaboratorResponse response = collaboratorService.respondToInvitation(tripId, collabId, accept, user);
            return ResponseEntity.ok(ApiResponse.success(accept ? "Invitation accepted" : "Invitation declined", response));
        }

        if (body.containsKey("role")) {
            CollaboratorRole newRole = CollaboratorRole.valueOf(body.get("role"));
            CollaboratorResponse response = collaboratorService.updateRole(tripId, collabId, newRole, user);
            return ResponseEntity.ok(ApiResponse.success("Role updated", response));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("Please provide 'accept' or 'role'"));
    }

    @DeleteMapping("/{collabId}")
    @Operation(summary = "Remove collaborator from trip")
    public ResponseEntity<ApiResponse<Void>> removeCollaborator(
            @PathVariable Long tripId,
            @PathVariable Long collabId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        collaboratorService.removeCollaborator(tripId, collabId, user);
        return ResponseEntity.ok(ApiResponse.success("Collaborator removed", null));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending invitations for the current user (path tripId is ignored, use 0)")
    public ResponseEntity<ApiResponse<List<CollaboratorResponse>>> getPendingInvitations(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(collaboratorService.getPendingInvitations(user)));
    }
}
