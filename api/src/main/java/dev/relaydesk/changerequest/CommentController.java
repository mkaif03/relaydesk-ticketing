package dev.relaydesk.changerequest;

import dev.relaydesk.changerequest.dto.CommentDto;
import dev.relaydesk.changerequest.dto.CommentRequest;
import dev.relaydesk.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/change-requests/{changeRequestId}/comments")
public class CommentController {

    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @GetMapping
    public List<CommentDto> list(@PathVariable UUID changeRequestId) {
        return service.listForChangeRequest(changeRequestId);
    }

    @PostMapping
    public CommentDto create(@PathVariable UUID changeRequestId,
                             @Valid @RequestBody CommentRequest req,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpServletRequest request) {
        return service.addComment(changeRequestId, req, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }
}
