package com.voyagecraft.service;

import com.voyagecraft.dto.collaborator.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripCollaboratorServiceTest {

    @InjectMocks private TripCollaboratorService service;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserRepository userRepository;

    private User owner, invitee;
    private Trip testTrip;
    private TripCollaborator testCollab;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@test.com").firstName("Owner").lastName("User").build();
        invitee = User.builder().id(2L).email("invitee@test.com").firstName("Invitee").lastName("User").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(owner).build();
        testCollab = TripCollaborator.builder().id(10L).trip(testTrip).user(invitee)
                .role(CollaboratorRole.EDITOR).invitationStatus(InvitationStatus.PENDING)
                .invitedBy(owner).invitedAt(LocalDateTime.now()).build();
    }

    @Test
    void inviteCollaborator_success() {
        CollaboratorRequest req = new CollaboratorRequest();
        req.setEmail("invitee@test.com"); req.setRole(CollaboratorRole.EDITOR);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("invitee@test.com")).thenReturn(Optional.of(invitee));
        when(collaboratorRepository.existsByTripIdAndUserId(1L, 2L)).thenReturn(false);
        when(collaboratorRepository.save(any())).thenReturn(testCollab);

        CollaboratorResponse resp = service.inviteCollaborator(1L, req, owner);
        assertNotNull(resp);
        assertEquals(CollaboratorRole.EDITOR, resp.getRole());
    }

    @Test
    void inviteCollaborator_notOwner_throwsException() {
        CollaboratorRequest req = new CollaboratorRequest();
        req.setEmail("x@test.com"); req.setRole(CollaboratorRole.VIEWER);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        assertThrows(UnauthorizedException.class, () -> service.inviteCollaborator(1L, req, invitee));
    }

    @Test
    void inviteCollaborator_alreadyExists_throwsException() {
        CollaboratorRequest req = new CollaboratorRequest();
        req.setEmail("invitee@test.com"); req.setRole(CollaboratorRole.EDITOR);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("invitee@test.com")).thenReturn(Optional.of(invitee));
        when(collaboratorRepository.existsByTripIdAndUserId(1L, 2L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.inviteCollaborator(1L, req, owner));
    }

    @Test
    void inviteCollaborator_userNotFound_throwsException() {
        CollaboratorRequest req = new CollaboratorRequest();
        req.setEmail("unknown@test.com"); req.setRole(CollaboratorRole.VIEWER);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.inviteCollaborator(1L, req, owner));
    }

    @Test
    void getCollaborators_success() {
        when(collaboratorRepository.findByTripId(1L)).thenReturn(List.of(testCollab));

        List<CollaboratorResponse> result = service.getCollaborators(1L);
        assertEquals(1, result.size());
    }

    @Test
    void respondToInvitation_accept_success() {
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));
        when(collaboratorRepository.save(any())).thenReturn(testCollab);

        CollaboratorResponse resp = service.respondToInvitation(1L, 10L, true, invitee);
        assertNotNull(resp);
    }

    @Test
    void respondToInvitation_decline_success() {
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));
        when(collaboratorRepository.save(any())).thenReturn(testCollab);

        CollaboratorResponse resp = service.respondToInvitation(1L, 10L, false, invitee);
        assertNotNull(resp);
    }

    @Test
    void respondToInvitation_notOwnInvitation_throwsException() {
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));
        assertThrows(UnauthorizedException.class, () -> service.respondToInvitation(1L, 10L, true, owner));
    }

    @Test
    void updateRole_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));
        when(collaboratorRepository.save(any())).thenReturn(testCollab);

        CollaboratorResponse resp = service.updateRole(1L, 10L, CollaboratorRole.VIEWER, owner);
        assertNotNull(resp);
    }

    @Test
    void updateRole_notOwner_throwsException() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        assertThrows(UnauthorizedException.class, () -> service.updateRole(1L, 10L, CollaboratorRole.VIEWER, invitee));
    }

    @Test
    void removeCollaborator_byOwner_success() {
        testCollab.setRole(CollaboratorRole.EDITOR);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));

        service.removeCollaborator(1L, 10L, owner);
        verify(collaboratorRepository).delete(testCollab);
    }

    @Test
    void removeCollaborator_cannotRemoveOwner() {
        testCollab.setRole(CollaboratorRole.OWNER);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));

        assertThrows(BadRequestException.class, () -> service.removeCollaborator(1L, 10L, owner));
    }

    @Test
    void removeCollaborator_noPermission_throwsException() {
        User third = User.builder().id(3L).email("third@test.com").firstName("X").lastName("Y").build();
        testCollab.setRole(CollaboratorRole.EDITOR);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findById(10L)).thenReturn(Optional.of(testCollab));

        assertThrows(UnauthorizedException.class, () -> service.removeCollaborator(1L, 10L, third));
    }

    @Test
    void getPendingInvitations_success() {
        when(collaboratorRepository.findByUserIdAndInvitationStatus(2L, InvitationStatus.PENDING))
                .thenReturn(List.of(testCollab));

        List<CollaboratorResponse> result = service.getPendingInvitations(invitee);
        assertEquals(1, result.size());
    }
}
