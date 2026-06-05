package com.voyagecraft.service;

import com.voyagecraft.dto.auth.UserResponse;
import com.voyagecraft.dto.collaborator.CollaboratorRequest;
import com.voyagecraft.dto.collaborator.CollaboratorResponse;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.TripCollaborator;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import com.voyagecraft.exception.BadRequestException;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import com.voyagecraft.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripCollaboratorService {

    private final TripCollaboratorRepository collaboratorRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public CollaboratorResponse inviteCollaborator(Long tripId, CollaboratorRequest request, User inviter) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (!trip.getCreatedBy().getId().equals(inviter.getId())) {
            throw new UnauthorizedException("Only the trip owner can invite collaborators");
        }

        User invitee = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (collaboratorRepository.existsByTripIdAndUserId(tripId, invitee.getId())) {
            throw new BadRequestException("User is already a collaborator on this trip");
        }

        TripCollaborator collaborator = TripCollaborator.builder()
                .trip(trip)
                .user(invitee)
                .role(request.getRole())
                .invitationStatus(InvitationStatus.PENDING)
                .invitedBy(inviter)
                .invitedAt(LocalDateTime.now())
                .build();

        collaborator = collaboratorRepository.save(collaborator);
        return mapToResponse(collaborator);
    }

    @Transactional(readOnly = true)
    public List<CollaboratorResponse> getCollaborators(Long tripId) {
        return collaboratorRepository.findByTripId(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CollaboratorResponse respondToInvitation(Long tripId, Long collabId, boolean accept, User user) {
        TripCollaborator collaborator = collaboratorRepository.findById(collabId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));

        if (!collaborator.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only respond to your own invitations");
        }

        collaborator.setInvitationStatus(accept ? InvitationStatus.ACCEPTED : InvitationStatus.DECLINED);
        collaborator.setRespondedAt(LocalDateTime.now());
        collaborator = collaboratorRepository.save(collaborator);
        return mapToResponse(collaborator);
    }

    @Transactional
    public CollaboratorResponse updateRole(Long tripId, Long collabId, CollaboratorRole newRole, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the trip owner can change roles");
        }

        TripCollaborator collaborator = collaboratorRepository.findById(collabId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));

        collaborator.setRole(newRole);
        collaborator = collaboratorRepository.save(collaborator);
        return mapToResponse(collaborator);
    }

    @Transactional
    public void removeCollaborator(Long tripId, Long collabId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        TripCollaborator collaborator = collaboratorRepository.findById(collabId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));

        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isSelf = collaborator.getUser().getId().equals(user.getId());

        if (!isOwner && !isSelf) {
            throw new UnauthorizedException("Only the trip owner or the collaborator themselves can remove");
        }

        if (collaborator.getRole() == CollaboratorRole.OWNER) {
            throw new BadRequestException("Cannot remove the trip owner");
        }

        collaboratorRepository.delete(collaborator);
    }

    @Transactional(readOnly = true)
    public List<CollaboratorResponse> getPendingInvitations(User user) {
        return collaboratorRepository.findByUserIdAndInvitationStatus(user.getId(), InvitationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CollaboratorResponse mapToResponse(TripCollaborator c) {
        UserResponse userResp = UserResponse.builder()
                .id(c.getUser().getId())
                .email(c.getUser().getEmail())
                .firstName(c.getUser().getFirstName())
                .lastName(c.getUser().getLastName())
                .avatarUrl(c.getUser().getAvatarUrl())
                .build();

        return CollaboratorResponse.builder()
                .id(c.getId())
                .user(userResp)
                .role(c.getRole())
                .invitationStatus(c.getInvitationStatus())
                .invitedAt(c.getInvitedAt())
                .respondedAt(c.getRespondedAt())
                .tripId(c.getTrip().getId())
                .tripTitle(c.getTrip().getTitle())
                .invitedByName(c.getInvitedBy() != null ? c.getInvitedBy().getFirstName() : null)
                .build();
    }
}
