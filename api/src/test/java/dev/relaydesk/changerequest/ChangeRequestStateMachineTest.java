package dev.relaydesk.changerequest;

import dev.relaydesk.common.IllegalTransitionException;
import dev.relaydesk.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChangeRequestStateMachineTest {

    private final ChangeRequestStateMachine stateMachine = new ChangeRequestStateMachine();
    private User admin;
    private User author;
    private User reviewer;
    private User releaseManager;
    private User randomUser;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole("ROLE_ADMIN");

        author = new User();
        author.setId(UUID.randomUUID());
        author.setRole("ROLE_ENGINEER");

        reviewer = new User();
        reviewer.setId(UUID.randomUUID());
        reviewer.setRole("ROLE_REVIEWER");

        releaseManager = new User();
        releaseManager.setId(UUID.randomUUID());
        releaseManager.setRole("ROLE_RELEASE_MANAGER");

        randomUser = new User();
        randomUser.setId(UUID.randomUUID());
        randomUser.setRole("ROLE_ENGINEER");
    }

    @Test
    void testValidTransition_DraftToSubmitted_AsAuthor() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author);

        stateMachine.transition(cr, "SUBMITTED", author);
        assertEquals("SUBMITTED", cr.getStatus());
    }

    @Test
    void testInvalidTransition_DraftToSubmitted_AsRandomUser() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author);

        assertThrows(AccessDeniedException.class, () -> stateMachine.transition(cr, "SUBMITTED", randomUser));
    }

    @Test
    void testInvalidTransition_DraftToApproved() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author);

        assertThrows(IllegalTransitionException.class, () -> stateMachine.transition(cr, "APPROVED", admin));
    }

    @Test
    void testGuard_SubmittedToApproved_WithoutApproval() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("SUBMITTED");

        assertThrows(IllegalTransitionException.class, () -> stateMachine.transition(cr, "APPROVED", reviewer));
    }

    @Test
    void testGuard_SubmittedToApproved_WithApproval_AsReviewer() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("SUBMITTED");
        
        Review review = new Review();
        review.setDecision("APPROVED");
        cr.setReviews(List.of(review));

        stateMachine.transition(cr, "APPROVED", reviewer);
        assertEquals("APPROVED", cr.getStatus());
    }

    @Test
    void testGuard_SubmittedToApproved_WithApproval_AsAuthor() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("SUBMITTED");
        
        Review review = new Review();
        review.setDecision("APPROVED");
        cr.setReviews(List.of(review));

        assertThrows(AccessDeniedException.class, () -> stateMachine.transition(cr, "APPROVED", author));
    }

    @Test
    void testTransition_SubmittedToRejected_AsReviewer() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("SUBMITTED");
        
        stateMachine.transition(cr, "REJECTED", reviewer);
        assertEquals("REJECTED", cr.getStatus());
    }

    @Test
    void testGuard_ApprovedToScheduled_WithoutDates() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("APPROVED");

        assertThrows(IllegalTransitionException.class, () -> stateMachine.transition(cr, "SCHEDULED", releaseManager));
    }
    
    @Test
    void testGuard_ApprovedToScheduled_WithDates_AsReleaseManager() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("APPROVED");
        cr.setPlannedWindowStart(OffsetDateTime.now());
        cr.setPlannedWindowEnd(OffsetDateTime.now().plusDays(1));

        stateMachine.transition(cr, "SCHEDULED", releaseManager);
        assertEquals("SCHEDULED", cr.getStatus());
    }

    @Test
    void testGuard_ApprovedToScheduled_WithDates_AsReviewer() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("APPROVED");
        cr.setPlannedWindowStart(OffsetDateTime.now());
        cr.setPlannedWindowEnd(OffsetDateTime.now().plusDays(1));

        assertThrows(AccessDeniedException.class, () -> stateMachine.transition(cr, "SCHEDULED", reviewer));
    }

    @Test
    void testTransition_ApprovedToRejected_AsReviewer() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("APPROVED");

        stateMachine.transition(cr, "REJECTED", reviewer);
        assertEquals("REJECTED", cr.getStatus());
    }

    @Test
    void testTransition_ApprovedToCancelled_AsAuthor() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("APPROVED");
        cr.setAuthor(author);

        stateMachine.transition(cr, "CANCELLED", author);
        assertEquals("CANCELLED", cr.getStatus());
    }

    @Test
    void testTransition_AdminCanDoAnything_ValidFlow() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author); 
        
        // Admin transitions DRAFT -> SUBMITTED 
        stateMachine.transition(cr, "SUBMITTED", admin);
        assertEquals("SUBMITTED", cr.getStatus());
        
        // Admin transitions SUBMITTED -> APPROVED 
        Review review = new Review();
        review.setDecision("APPROVED");
        cr.setReviews(List.of(review));
        stateMachine.transition(cr, "APPROVED", admin);
        assertEquals("APPROVED", cr.getStatus());
        
        // Admin transitions APPROVED -> CANCELLED
        stateMachine.transition(cr, "CANCELLED", admin);
        assertEquals("CANCELLED", cr.getStatus());
    }
}
