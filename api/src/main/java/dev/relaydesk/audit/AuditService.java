package dev.relaydesk.audit;

import dev.relaydesk.user.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public void logEvent(User actor, String action, String entityType, UUID entityId, String payload, String traceId) {
        AuditEvent event = new AuditEvent();
        event.setActor(actor);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);

        String jsonPayload = "{}";
        try {
            jsonPayload = mapper.writeValueAsString(java.util.Map.of("message", payload));
        } catch (Exception ignored) {}

        event.setPayload(jsonPayload);
        event.setTraceId(traceId);
        auditEventRepository.save(event);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public dev.relaydesk.common.PageResponseDto<AuditEventDto> listAll(
            String action, String entityType, org.springframework.data.domain.Pageable pageable, User actor) {
        
        if (!"ROLE_ADMIN".equals(actor.getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("Only Admins can view audit logs");
        }

        org.springframework.data.jpa.domain.Specification<AuditEvent> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (action != null && !action.isEmpty()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (entityType != null && !entityType.isEmpty()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<AuditEvent> page = auditEventRepository.findAll(spec, pageable);

        java.util.List<AuditEventDto> content = page.getContent().stream().map(this::toDto).collect(java.util.stream.Collectors.toList());

        return new dev.relaydesk.common.PageResponseDto<>(
                content, page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber()
        );
    }

    private AuditEventDto toDto(AuditEvent event) {
        AuditEventDto dto = new AuditEventDto();
        dto.setId(event.getId());
        dto.setActorEmail(event.getActor().getEmail());
        dto.setAction(event.getAction());
        dto.setEntityType(event.getEntityType());
        dto.setEntityId(event.getEntityId());
        dto.setPayload(event.getPayload());
        dto.setTraceId(event.getTraceId());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}
