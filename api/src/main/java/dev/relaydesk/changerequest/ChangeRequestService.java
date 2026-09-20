package dev.relaydesk.changerequest;

import dev.relaydesk.audit.AuditService;
import dev.relaydesk.changerequest.dto.ChangeRequestDto;
import dev.relaydesk.changerequest.dto.CreateChangeRequest;
import dev.relaydesk.changerequest.dto.UpdateChangeRequest;
import dev.relaydesk.common.NotFoundException;
import dev.relaydesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import java.time.OffsetDateTime;

@Service
public class ChangeRequestService {

    private final ChangeRequestRepository repository;
    private final ChangeRequestStateMachine stateMachine;
    private final AuditService auditService;

    public ChangeRequestService(ChangeRequestRepository repository,
                                ChangeRequestStateMachine stateMachine,
                                AuditService auditService) {
        this.repository = repository;
        this.stateMachine = stateMachine;
        this.auditService = auditService;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public dev.relaydesk.common.PageResponseDto<ChangeRequestDto> listAll(
            String status, String riskLevel, String environment, String authorEmail, String q, org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<ChangeRequest> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("author", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("team", jakarta.persistence.criteria.JoinType.LEFT);
            }

            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (riskLevel != null && !riskLevel.isEmpty()) {
                predicates.add(cb.equal(root.get("riskLevel"), riskLevel));
            }
            if (environment != null && !environment.isEmpty()) {
                predicates.add(cb.equal(root.get("environment"), environment));
            }
            if (authorEmail != null && !authorEmail.isEmpty()) {
                predicates.add(cb.equal(root.get("author").get("email"), authorEmail));
            }
            if (q != null && !q.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<ChangeRequest> page = repository.findAll(spec, pageable);

        List<ChangeRequestDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new dev.relaydesk.common.PageResponseDto<>(
                content, page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber()
        );
    }

    public ChangeRequestDto get(UUID id) {
        return toDto(findById(id));
    }

    @Transactional
    public ChangeRequestDto create(CreateChangeRequest req, User actor, String traceId) {
        if (!actor.getRole().equals("ROLE_ADMIN") && 
            !actor.getRole().equals("ROLE_ENGINEER") && 
            !actor.getRole().equals("ROLE_REVIEWER")) {
            throw new AccessDeniedException("You do not have permission to create a Change Request");
        }

        ChangeRequest cr = new ChangeRequest();
        cr.setTitle(req.getTitle());
        cr.setDescription(req.getDescription());
        cr.setPriority(req.getPriority());
        cr.setRiskLevel(req.getRiskLevel());
        cr.setService(req.getService());
        cr.setEnvironment(req.getEnvironment());
        cr.setPublicKey("CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        cr.setPlannedWindowStart(req.getScheduledStart());
        cr.setPlannedWindowEnd(req.getScheduledEnd());
        cr.setAuthor(actor);
        cr.setTeam(actor.getTeam());
        cr.setStatus("DRAFT");
        
        cr = repository.save(cr);
        auditService.logEvent(actor, "CR_CREATED", "ChangeRequest", cr.getId(), "Created DRAFT", traceId);
        return toDto(cr);
    }

    @Transactional
    public ChangeRequestDto update(UUID id, UpdateChangeRequest req, User actor, String traceId) {
        ChangeRequest cr = findById(id);
        
        if (!cr.getAuthor().getId().equals(actor.getId()) && !actor.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have permission to update this Change Request");
        }
        
        if (cr.getVersion() != req.getVersion()) {
            throw new dev.relaydesk.common.StaleVersionException("Change request was modified by someone else", cr.getVersion());
        }

        if (req.getTitle() != null) cr.setTitle(req.getTitle());
        if (req.getDescription() != null) cr.setDescription(req.getDescription());
        if (req.getPriority() != null) cr.setPriority(req.getPriority());
        if (req.getRiskLevel() != null) cr.setRiskLevel(req.getRiskLevel());
        if (req.getService() != null) cr.setService(req.getService());
        if (req.getEnvironment() != null) cr.setEnvironment(req.getEnvironment());
        if (req.getScheduledStart() != null) cr.setPlannedWindowStart(req.getScheduledStart());
        if (req.getScheduledEnd() != null) cr.setPlannedWindowEnd(req.getScheduledEnd());

        cr = repository.save(cr);
        auditService.logEvent(actor, "CR_UPDATED", "ChangeRequest", cr.getId(), "Updated fields", traceId);
        return toDto(cr);
    }

    @Transactional
    public ChangeRequestDto transition(UUID id, String targetStatus, long version, User actor, String traceId) {
        if (!java.util.Set.of("SUBMITTED", "APPROVED", "CHANGES_REQUESTED", "REJECTED", "CANCELLED").contains(targetStatus)) {
            throw new dev.relaydesk.common.IllegalTransitionException("Invalid target status for public transition API");
        }
        
        ChangeRequest cr = findById(id);
        if (cr.getVersion() != version) {
            throw new dev.relaydesk.common.StaleVersionException("Change request was modified by someone else", cr.getVersion());
        }

        String oldStatus = cr.getStatus();
        stateMachine.transition(cr, targetStatus, actor);
        
        cr = repository.save(cr);
        auditService.logEvent(actor, "CR_TRANSITION", "ChangeRequest", cr.getId(), oldStatus + " -> " + targetStatus, traceId);
        return toDto(cr);
    }

    @Transactional
    public void delete(UUID id, User actor, String traceId) {
        ChangeRequest cr = findById(id);
        if (!actor.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have permission to delete this Change Request. Only ADMIN can delete.");
        }
        cr.setDeletedAt(OffsetDateTime.now());
        repository.save(cr);
        auditService.logEvent(actor, "CR_DELETED", "ChangeRequest", id, "Soft-deleted CR", traceId);
    }

    private ChangeRequest findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ChangeRequest not found"));
    }

    private ChangeRequestDto toDto(ChangeRequest cr) {
        ChangeRequestDto dto = new ChangeRequestDto();
        dto.setId(cr.getId());
        dto.setPublicKey(cr.getPublicKey());
        dto.setTitle(cr.getTitle());
        dto.setDescription(cr.getDescription());
        dto.setStatus(cr.getStatus());
        dto.setPriority(cr.getPriority());
        dto.setRiskLevel(cr.getRiskLevel());
        dto.setService(cr.getService());
        dto.setEnvironment(cr.getEnvironment());
        dto.setAuthorId(cr.getAuthor().getId());
        dto.setTeamId(cr.getTeam() != null ? cr.getTeam().getId() : null);
        dto.setScheduledStart(cr.getPlannedWindowStart());
        dto.setScheduledEnd(cr.getPlannedWindowEnd());
        dto.setVersion(cr.getVersion());
        dto.setCreatedAt(cr.getCreatedAt());
        dto.setUpdatedAt(cr.getUpdatedAt());
        return dto;
    }
}
