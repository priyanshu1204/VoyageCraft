package com.voyagecraft.service;

import com.voyagecraft.dto.collab.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CollaborationServiceTest {
    @InjectMocks private CollaborationService service;
    @Mock private PollRepository pollRepository;
    @Mock private PollVoteRepository voteRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ChangeLogRepository changeLogRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    private User testUser; private Trip testTrip; private Poll testPoll;
    @BeforeEach void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser).createdAt(LocalDateTime.now()).build();
        testTrip.setCollaborators(new ArrayList<>());
        PollOption opt = PollOption.builder().id(1L).optionText("A").votes(new ArrayList<>()).build();
        testPoll = Poll.builder().id(1L).trip(testTrip).question("Where?").createdBy(testUser)
                .status(PollStatus.OPEN).allowMultipleVotes(false)
                .options(new ArrayList<>(List.of(opt))).createdAt(LocalDateTime.now()).build();
        opt.setPoll(testPoll);
    }
    @Test void createPoll_ok() {
        PollRequest r=new PollRequest(); r.setTripId(1L); r.setQuestion("Where?"); r.setOptions(List.of("A","B"));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(pollRepository.save(any())).thenReturn(testPoll);
        when(changeLogRepository.save(any())).thenReturn(ChangeLog.builder().id(1L).build());
        assertNotNull(service.createPoll(r,testUser));
    }
    @Test void createPoll_notFound() {
        PollRequest r=new PollRequest(); r.setTripId(99L); r.setQuestion("X"); r.setOptions(List.of("A"));
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,()->service.createPoll(r,testUser));
    }
    @Test void getTripPolls_ok() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(pollRepository.findByTripIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testPoll));
        assertEquals(1,service.getTripPolls(1L,testUser).size());
    }
    @Test void closePoll_ok() {
        when(pollRepository.findById(1L)).thenReturn(Optional.of(testPoll));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(pollRepository.save(any())).thenReturn(testPoll);
        assertNotNull(service.closePoll(1L,testUser));
    }
    @Test void closePoll_notFound() { when(pollRepository.findById(99L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->service.closePoll(99L,testUser)); }
    @Test void deletePoll_ok() {
        when(pollRepository.findById(1L)).thenReturn(Optional.of(testPoll));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        service.deletePoll(1L,testUser); verify(pollRepository).delete(testPoll);
    }
    @Test void vote_ok() {
        when(pollRepository.findAll()).thenReturn(List.of(testPoll));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(voteRepository.findByPollOptionIdAndUserId(1L,1L)).thenReturn(Optional.empty());
        when(voteRepository.save(any())).thenReturn(PollVote.builder().id(1L).build());
        when(pollRepository.findById(1L)).thenReturn(Optional.of(testPoll));
        when(changeLogRepository.save(any())).thenReturn(ChangeLog.builder().id(1L).build());
        assertNotNull(service.vote(1L,testUser));
    }
    @Test void vote_toggleRemove() {
        PollVote existing=PollVote.builder().id(1L).pollOption(testPoll.getOptions().get(0)).user(testUser).build();
        when(pollRepository.findAll()).thenReturn(List.of(testPoll));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(voteRepository.findByPollOptionIdAndUserId(1L,1L)).thenReturn(Optional.of(existing));
        when(pollRepository.findById(1L)).thenReturn(Optional.of(testPoll));
        when(changeLogRepository.save(any())).thenReturn(ChangeLog.builder().id(1L).build());
        assertNotNull(service.vote(1L,testUser));
    }
    @Test void addComment_ok() {
        CommentRequest r=new CommentRequest(); r.setTripId(1L); r.setContent("Nice!"); r.setItemType("activity"); r.setItemId(1L);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        Comment saved=Comment.builder().id(1L).trip(testTrip).user(testUser).content("Nice!")
                .itemType("activity").itemId(1L).createdAt(LocalDateTime.now()).build();
        when(commentRepository.save(any())).thenReturn(saved);
        when(changeLogRepository.save(any())).thenReturn(ChangeLog.builder().id(1L).build());
        assertNotNull(service.addComment(r,testUser));
    }
    @Test void addComment_withParent() {
        Comment parent=Comment.builder().id(1L).trip(testTrip).user(testUser).content("X").createdAt(LocalDateTime.now()).build();
        CommentRequest r=new CommentRequest(); r.setTripId(1L); r.setContent("Reply"); r.setItemType("activity"); r.setItemId(1L); r.setParentId(1L);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any())).thenReturn(parent);
        when(changeLogRepository.save(any())).thenReturn(ChangeLog.builder().id(1L).build());
        assertNotNull(service.addComment(r,testUser));
    }
    @Test void getComments_ok() {
        Comment c=Comment.builder().id(1L).trip(testTrip).user(testUser).content("X")
                .itemType("activity").itemId(1L).createdAt(LocalDateTime.now()).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(commentRepository.findByTripIdAndItemTypeAndItemIdAndParentIsNullOrderByCreatedAtDesc(1L,"activity",1L)).thenReturn(List.of(c));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(1L)).thenReturn(Collections.emptyList());
        assertEquals(1,service.getItemComments(1L,"activity",1L,testUser).size());
    }
    @Test void deleteComment_ok() {
        Comment c=Comment.builder().id(1L).trip(testTrip).user(testUser).content("X").createdAt(LocalDateTime.now()).build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(c));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        service.deleteComment(1L,testUser); verify(commentRepository).delete(c);
    }
    @Test void getChangeLog_ok() {
        ChangeLog cl=ChangeLog.builder().id(1L).trip(testTrip).user(testUser).action(ChangeAction.CREATED)
                .entityType("trip").entityId(1L).description("Created").createdAt(LocalDateTime.now()).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(changeLogRepository.findTop50ByTripIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(cl));
        assertEquals(1,service.getChangeLog(1L,testUser).size());
    }
    @Test void logChange_ok() {
        when(changeLogRepository.save(any())).thenReturn(ChangeLog.builder().id(1L).build());
        assertDoesNotThrow(()->service.logChange(testTrip,testUser,ChangeAction.CREATED,"Trip",1L,"Created"));
    }
    @Test void logChange_exceptionSwallowed() {
        when(changeLogRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertDoesNotThrow(()->service.logChange(testTrip,testUser,ChangeAction.CREATED,"Trip",1L,"Created"));
    }
    @Test void getUserRole_owner() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        assertEquals("OWNER",service.getUserRole(1L,testUser));
    }
    @Test void getUserRole_viewer() {
        User o=User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        assertEquals("VIEWER",service.getUserRole(1L,o));
    }
}
