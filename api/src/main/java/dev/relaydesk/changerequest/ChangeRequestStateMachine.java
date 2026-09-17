package dev.relaydesk.changerequest;

import dev.relaydesk.common.IllegalTransitionException;
import dev.relaydesk.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ChangeRequestStateMachine {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "DRAFT", Set.of("IN_REVIEW"),
            "IN_REVIEW", Set.of("APPROVED", "REJECTED", "DRAFT"),
            "REJECTED", Set.of("DRAFT"),
            "APPROVED", Set.of("SCHEDULED", "DRAFT"),
            "SCHEDULED", Set.of("IN_PROGRESS", "DRAFT"),
            "IN_PROGRESS", Set.of("COMPLETED", "FAILED"),
            "COMPLETED", Set.of("FAILED"),
            "FAILED", Set.of("DRAFT", "IN_PROGRESS")
    );

    public void transition(ChangeRequest cr, String targetStatus, User actor) {
        String currentStatus = cr.getStatus();
        
        if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new IllegalTransitionException("Cannot transition from " + currentStatus + " to " + targetStatus);
        }

        boolean isAdmin = "ROLE_ADMIN".equals(actor.getRole());

        if (("IN_REVIEW".equals(targetStatus) || ("DRAFT".equals(targetStatus) && !"DRAFT".equals(currentStatus))) && !isAdmin) {
            if (!cr.getAuthor().getId().equals(actor.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Only the author or an admin can transition to " + targetStatus);
            }
        }

        if (("APPROVED".equals(targetStatus) || "REJECTED".equals(targetStatus)) && !isAdmin) {
            if (!"ROLE_REVIEWER".equals(actor.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Only a Reviewer or Admin can transition to " + targetStatus);
            }
        }

        if (("SCHEDULED".equals(targetStatus) || "IN_PROGRESS".equals(targetStatus) || 
             "COMPLETED".equals(targetStatus) || "FAILED".equals(targetStatus)) && !isAdmin) {
            if (!"ROLE_RELEASE_MANAGER".equals(actor.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Only a Release Manager or Admin can transition to " + targetStatus);
            }
        }

        if ("APPROVED".equals(targetStatus)) {
            if (cr.getReviews() == null || cr.getReviews().stream().noneMatch(r -> "APPROVED".equals(r.getDecision()))) {
                throw new IllegalTransitionException("Cannot transition to APPROVED without at least one APPROVED review");
            }
            if (cr.getReviews().stream().anyMatch(r -> "REJECTED".equals(r.getDecision()))) {
                throw new IllegalTransitionException("Cannot transition to APPROVED while there is a REJECTED review");
            }
        }
        
        if ("SCHEDULED".equals(targetStatus)) {
            if (cr.getPlannedWindowStart() == null || cr.getPlannedWindowEnd() == null) {
                throw new IllegalTransitionException("Cannot transition to SCHEDULED without planned window dates");
            }
        }

        cr.setStatus(targetStatus);
    }
}
