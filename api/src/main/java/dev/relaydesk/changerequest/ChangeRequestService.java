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

    public List<ChangeRequestDto> listAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
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
        
        cr.setVersion(req.getVersion());

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
        ChangeRequest cr = findById(id);
        cr.setVersion(version);

        String oldStatus = cr.getStatus();
        stateMachine.transition(cr, targetStatus, actor);
        
        cr = repository.save(cr);
        auditService.logEvent(actor, "CR_TRANSITION", "ChangeRequest", cr.getId(), oldStatus + " -> " + targetStatus, traceId);
        return toDto(cr);
    }

    @Transactional
    public void delete(UUID id, User actor, String traceId) {
        ChangeRequest cr = findById(id);
        if (!cr.getAuthor().getId().equals(actor.getId()) && !actor.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have permission to delete this Change Request");
        }
        repository.delete(cr);
        auditService.logEvent(actor, "CR_DELETED", "ChangeRequest", id, "Deleted CR", traceId);
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
