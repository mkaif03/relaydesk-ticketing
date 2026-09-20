package dev.relaydesk.release;

import dev.relaydesk.audit.AuditService;
import dev.relaydesk.changerequest.ChangeRequest;
import dev.relaydesk.changerequest.ChangeRequestRepository;
import dev.relaydesk.changerequest.ChangeRequestStateMachine;
import dev.relaydesk.common.IllegalTransitionException;
import dev.relaydesk.common.NotFoundException;
import dev.relaydesk.release.dto.ReleaseDto;
import dev.relaydesk.release.dto.ReleaseItemDto;
import dev.relaydesk.release.dto.ReleaseItemRequest;
import dev.relaydesk.release.dto.ReleaseRequest;
import dev.relaydesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ReleaseItemRepository releaseItemRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final ChangeRequestStateMachine stateMachine;
    private final AuditService auditService;

    public ReleaseService(ReleaseRepository releaseRepository,
                          ReleaseItemRepository releaseItemRepository,
                          ChangeRequestRepository changeRequestRepository,
                          ChangeRequestStateMachine stateMachine,
                          AuditService auditService) {
        this.releaseRepository = releaseRepository;
        this.releaseItemRepository = releaseItemRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.stateMachine = stateMachine;
        this.auditService = auditService;
    }

    public List<ReleaseDto> listReleases() {
        return releaseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ReleaseDto getRelease(UUID releaseId) {
        return toDto(releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NotFoundException("Release not found")));
    }

    @Transactional
    public ReleaseDto createRelease(ReleaseRequest req, User manager, String traceId) {
        Release release = new Release();
        release.setName(req.getName());
        release.setNotes(req.getNotes());
        release.setStatus("PLANNED");
        release.setManager(manager);
        
        release = releaseRepository.save(release);
        auditService.logEvent(manager, "RELEASE_CREATED", "Release", release.getId(), "Release created: " + release.getName(), traceId);
        
        return toDto(release);
    }

    @Transactional
    public ReleaseItemDto addChangeRequest(UUID releaseId, ReleaseItemRequest req, User addedBy, String traceId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NotFoundException("Release not found"));
        
        if (!"PLANNED".equals(release.getStatus())) {
            throw new IllegalTransitionException("Cannot add items to a release that is not PLANNED");
        }

        ChangeRequest cr = changeRequestRepository.findById(req.getChangeRequestId())
                .orElseThrow(() -> new NotFoundException("ChangeRequest not found"));

        if (!"APPROVED".equals(cr.getStatus())) {
            throw new IllegalTransitionException("ChangeRequest must be in APPROVED state to be added to a release");
        }
        
        stateMachine.transition(cr, "SCHEDULED", addedBy);
        changeRequestRepository.save(cr);

        ReleaseItem item = new ReleaseItem();
        item.setRelease(release);
        item.setChangeRequest(cr);
        item.setAddedBy(addedBy);

        item = releaseItemRepository.save(item);
        auditService.logEvent(addedBy, "RELEASE_ITEM_ADDED", "Release", release.getId(), "Added CR: " + cr.getId(), traceId);
        
        return toItemDto(item);
    }

    public List<ReleaseItemDto> listItems(UUID releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NotFoundException("Release not found"));
        return releaseItemRepository.findByRelease(release).stream().map(this::toItemDto).collect(Collectors.toList());
    }

    @Transactional
    public ReleaseDto shipRelease(UUID releaseId, long version, User actor, String traceId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NotFoundException("Release not found"));
        
        if (release.getVersion() != version) {
            throw new dev.relaydesk.common.StaleVersionException("Stale release version", release.getVersion());
        }
        
        if (!"PLANNED".equals(release.getStatus())) {
            throw new IllegalTransitionException("Only PLANNED releases can be shipped");
        }

        release.setStatus("SHIPPED");
        release.setShippedAt(OffsetDateTime.now());
        release = releaseRepository.save(release);

        List<ReleaseItem> items = releaseItemRepository.findByRelease(release);
        for (ReleaseItem item : items) {
            if (item.isActive()) {
                ChangeRequest cr = item.getChangeRequest();
                stateMachine.transition(cr, "SHIPPED", actor);
                changeRequestRepository.save(cr);
                auditService.logEvent(actor, "CR_SHIPPED", "ChangeRequest", cr.getId(), "CR shipped via release " + release.getName(), traceId);
            }
        }

        auditService.logEvent(actor, "RELEASE_SHIPPED", "Release", release.getId(), "Release shipped", traceId);
        return toDto(release);
    }

    @Transactional
    public ReleaseDto rollbackRelease(UUID releaseId, long version, User actor, String traceId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NotFoundException("Release not found"));
        
        if (release.getVersion() != version) {
            throw new dev.relaydesk.common.StaleVersionException("Stale release version", release.getVersion());
        }
        
        if (!"SHIPPED".equals(release.getStatus())) {
            throw new IllegalTransitionException("Only SHIPPED releases can be rolled back");
        }

        release.setStatus("ROLLED_BACK");
        release.setRolledBackAt(OffsetDateTime.now());
        release = releaseRepository.save(release);

        List<ReleaseItem> items = releaseItemRepository.findByRelease(release);
        for (ReleaseItem item : items) {
            if (item.isActive()) {
                ChangeRequest cr = item.getChangeRequest();
                // State machine does not support transitioning out of SHIPPED. 
                // We leave the CR as SHIPPED.
                auditService.logEvent(actor, "CR_ROLLED_BACK", "ChangeRequest", cr.getId(), "CR rolled back via release " + release.getName(), traceId);
            }
        }

        auditService.logEvent(actor, "RELEASE_ROLLED_BACK", "Release", release.getId(), "Release rolled back", traceId);
        return toDto(release);
    }

    private ReleaseDto toDto(Release release) {
        ReleaseDto dto = new ReleaseDto();
        dto.setId(release.getId());
        dto.setName(release.getName());
        dto.setStatus(release.getStatus());
        dto.setManagerId(release.getManager().getId());
        dto.setShippedAt(release.getShippedAt());
        dto.setRolledBackAt(release.getRolledBackAt());
        dto.setNotes(release.getNotes());
        dto.setVersion(release.getVersion());
        dto.setCreatedAt(release.getCreatedAt());
        return dto;
    }

    private ReleaseItemDto toItemDto(ReleaseItem item) {
        ReleaseItemDto dto = new ReleaseItemDto();
        dto.setId(item.getId());
        dto.setChangeRequestId(item.getChangeRequest().getId());
        dto.setReleaseId(item.getRelease().getId());
        dto.setAddedById(item.getAddedBy().getId());
        dto.setActive(item.isActive());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }
}
