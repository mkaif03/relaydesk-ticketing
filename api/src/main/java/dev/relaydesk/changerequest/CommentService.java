package dev.relaydesk.changerequest;

import dev.relaydesk.audit.AuditService;
import dev.relaydesk.changerequest.dto.CommentDto;
import dev.relaydesk.changerequest.dto.CommentRequest;
import dev.relaydesk.common.NotFoundException;
import dev.relaydesk.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final AuditService auditService;

    public CommentService(CommentRepository commentRepository,
                          ChangeRequestRepository changeRequestRepository,
                          AuditService auditService) {
        this.commentRepository = commentRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listForChangeRequest(UUID changeRequestId) {
        return changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new NotFoundException("ChangeRequest not found"))
                .getComments().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(UUID changeRequestId, CommentRequest req, User author, String traceId) {
        ChangeRequest cr = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new NotFoundException("ChangeRequest not found"));

        Comment comment = new Comment();
        comment.setChangeRequest(cr);
        comment.setAuthor(author);
        comment.setBody(req.getContent());

        comment = commentRepository.save(comment);
        auditService.logEvent(author, "COMMENT_ADDED", "ChangeRequest", cr.getId(), "Comment added", traceId);
        
        return toDto(comment);
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setChangeRequestId(comment.getChangeRequest().getId());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setContent(comment.getBody());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
