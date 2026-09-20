package dev.relaydesk.changerequest;

import dev.relaydesk.audit.AuditService;
import dev.relaydesk.changerequest.dto.ReviewDto;
import dev.relaydesk.changerequest.dto.ReviewRequest;
import dev.relaydesk.common.NotFoundException;
import dev.relaydesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final AuditService auditService;

    public ReviewService(ReviewRepository reviewRepository,
                         ChangeRequestRepository changeRequestRepository,
                         AuditService auditService) {
        this.reviewRepository = reviewRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listForChangeRequest(UUID changeRequestId) {
        return changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new NotFoundException("ChangeRequest not found"))
                .getReviews().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto addReview(UUID changeRequestId, ReviewRequest req, User reviewer, String traceId) {
        if (!reviewer.getRole().equals("ROLE_ADMIN") && !reviewer.getRole().equals("ROLE_REVIEWER")) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to leave a review");
        }

        ChangeRequest cr = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new NotFoundException("ChangeRequest not found"));
        
        if (cr.getAuthor().getId().equals(reviewer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot approve or review your own change request");
        }
        
        Review review = new Review();
        review.setChangeRequest(cr);
        review.setReviewer(reviewer);
        review.setDecision(req.getStatus());
        review.setComment(req.getNotes());
        
        review = reviewRepository.save(review);
        auditService.logEvent(reviewer, "REVIEW_ADDED", "ChangeRequest", cr.getId(), "Review added: " + req.getStatus(), traceId);
        
        return toDto(review);
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setChangeRequestId(review.getChangeRequest().getId());
        dto.setReviewerId(review.getReviewer().getId());
        dto.setStatus(review.getDecision());
        dto.setNotes(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
