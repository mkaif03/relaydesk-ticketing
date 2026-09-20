package dev.relaydesk.changerequest;

import dev.relaydesk.common.IllegalTransitionException;
import dev.relaydesk.user.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ChangeRequestStateMachine {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "DRAFT", Set.of("SUBMITTED"),
            "SUBMITTED", Set.of("APPROVED", "CHANGES_REQUESTED", "REJECTED"),
            "CHANGES_REQUESTED", Set.of("SUBMITTED"),
            "APPROVED", Set.of("SCHEDULED", "REJECTED", "CANCELLED"),
            "SCHEDULED", Set.of("SHIPPED")
    );

    public void transition(ChangeRequest cr, String targetStatus, User actor) {
        String currentStatus = cr.getStatus();
        
        if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new IllegalTransitionException("Cannot transition from " + currentStatus + " to " + targetStatus);
        }

        boolean isAdmin = "ROLE_ADMIN".equals(actor.getRole());

        // SUBMITTED logic
        if ("SUBMITTED".equals(targetStatus) && !isAdmin) {
            if (!cr.getAuthor().getId().equals(actor.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Only the author or an admin can transition to " + targetStatus);
            }
        }

        // CANCELLED logic
        if ("CANCELLED".equals(targetStatus) && !isAdmin) {
            if (!cr.getAuthor().getId().equals(actor.getId()) && !"ROLE_REVIEWER".equals(actor.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Only the author, reviewer, or an admin can transition to CANCELLED");
            }
        }

        // APPROVED, CHANGES_REQUESTED, REJECTED logic
        if (("APPROVED".equals(targetStatus) || "CHANGES_REQUESTED".equals(targetStatus) || "REJECTED".equals(targetStatus))) {
            if (!isAdmin && !"ROLE_REVIEWER".equals(actor.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Only a Reviewer or Admin can transition to " + targetStatus);
            }
            if (cr.getAuthor().getId().equals(actor.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("You cannot approve, reject, or request changes on your own change request");
            }
        }

        // SCHEDULED, SHIPPED logic
        if (("SCHEDULED".equals(targetStatus) || "SHIPPED".equals(targetStatus)) && !isAdmin) {
            if (!"ROLE_RELEASE_MANAGER".equals(actor.getRole())) {
                throw new org.springframework.security.access.AccessDeniedException("Only a Release Manager or Admin can transition to " + targetStatus);
            }
        }

        if ("APPROVED".equals(targetStatus)) {
            if (cr.getReviews() == null || cr.getReviews().stream().noneMatch(r -> "APPROVED".equals(r.getDecision()))) {
                throw new IllegalTransitionException("Cannot transition to APPROVED without at least one APPROVED review");
            }
            if (cr.getReviews().stream().anyMatch(r -> "CHANGES_REQUESTED".equals(r.getDecision()) || "REJECTED".equals(r.getDecision()))) {
                throw new IllegalTransitionException("Cannot transition to APPROVED while there is a CHANGES_REQUESTED or REJECTED review");
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
