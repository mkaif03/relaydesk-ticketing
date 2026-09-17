package dev.relaydesk.changerequest;

import dev.relaydesk.changerequest.dto.ReviewDto;
import dev.relaydesk.changerequest.dto.ReviewRequest;
import dev.relaydesk.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/change-requests/{changeRequestId}/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReviewDto> list(@PathVariable UUID changeRequestId) {
        return service.listForChangeRequest(changeRequestId);
    }

    @PostMapping
    public ReviewDto create(@PathVariable UUID changeRequestId,
                            @Valid @RequestBody ReviewRequest req,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            HttpServletRequest request) {
        return service.addReview(changeRequestId, req, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }
}
