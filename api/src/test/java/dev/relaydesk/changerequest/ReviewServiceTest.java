package dev.relaydesk.changerequest;

import dev.relaydesk.audit.AuditService;
import dev.relaydesk.changerequest.dto.ReviewRequest;
import dev.relaydesk.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ChangeRequestRepository changeRequestRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelfApprovalImpossible() {
        // Given
        UUID changeRequestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // The reviewer is the same as the author
        User authorAndReviewer = new User();
        authorAndReviewer.setId(userId);
        authorAndReviewer.setRole("ROLE_REVIEWER"); // Role check passes

        ChangeRequest cr = new ChangeRequest();
        cr.setId(changeRequestId);
        cr.setAuthor(authorAndReviewer);

        ReviewRequest req = new ReviewRequest();
        req.setStatus("APPROVED");

        when(changeRequestRepository.findById(changeRequestId)).thenReturn(Optional.of(cr));

        // When & Then
        // The role check will pass, but the authorship check must throw AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> 
            reviewService.addReview(changeRequestId, req, authorAndReviewer, "trace-id")
        );
    }
}
