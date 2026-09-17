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
    void testValidTransition_DraftToInReview_AsAuthor() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author);

        stateMachine.transition(cr, "IN_REVIEW", author);
        assertEquals("IN_REVIEW", cr.getStatus());
    }

    @Test
    void testInvalidTransition_DraftToInReview_AsRandomUser() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author);

        assertThrows(AccessDeniedException.class, () -> stateMachine.transition(cr, "IN_REVIEW", randomUser));
    }

    @Test
    void testInvalidTransition_DraftToApproved() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author);

        assertThrows(IllegalTransitionException.class, () -> stateMachine.transition(cr, "APPROVED", admin));
    }

    @Test
    void testGuard_InReviewToApproved_WithoutApproval() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("IN_REVIEW");

        assertThrows(IllegalTransitionException.class, () -> stateMachine.transition(cr, "APPROVED", reviewer));
    }

    @Test
    void testGuard_InReviewToApproved_WithApproval_AsReviewer() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("IN_REVIEW");
        
        Review review = new Review();
        review.setDecision("APPROVED");
        cr.setReviews(List.of(review));

        stateMachine.transition(cr, "APPROVED", reviewer);
        assertEquals("APPROVED", cr.getStatus());
    }

    @Test
    void testGuard_InReviewToApproved_WithApproval_AsAuthor() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("IN_REVIEW");
        
        Review review = new Review();
        review.setDecision("APPROVED");
        cr.setReviews(List.of(review));

        assertThrows(AccessDeniedException.class, () -> stateMachine.transition(cr, "APPROVED", author));
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
    void testTransition_AdminCanDoAnything_ValidFlow() {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus("DRAFT");
        cr.setAuthor(author); // Even though admin transitions, author is set
        
        // Admin transitions DRAFT -> IN_REVIEW (ignoring author rule)
        stateMachine.transition(cr, "IN_REVIEW", admin);
        assertEquals("IN_REVIEW", cr.getStatus());
        
        // Admin transitions IN_REVIEW -> APPROVED (ignoring reviewer rule)
        Review review = new Review();
        review.setDecision("APPROVED");
        cr.setReviews(List.of(review));
        stateMachine.transition(cr, "APPROVED", admin);
        assertEquals("APPROVED", cr.getStatus());
    }
}
