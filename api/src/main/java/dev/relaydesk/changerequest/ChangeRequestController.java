package dev.relaydesk.changerequest;

import dev.relaydesk.changerequest.dto.ChangeRequestDto;
import dev.relaydesk.changerequest.dto.CreateChangeRequest;
import dev.relaydesk.changerequest.dto.UpdateChangeRequest;
import dev.relaydesk.changerequest.dto.TransitionRequest;
import dev.relaydesk.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/change-requests")
public class ChangeRequestController {

    private final ChangeRequestService service;

    public ChangeRequestController(ChangeRequestService service) {
        this.service = service;
    }

    @GetMapping
    public List<ChangeRequestDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ChangeRequestDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ChangeRequestDto create(@Valid @RequestBody CreateChangeRequest req,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   HttpServletRequest request) {
        return service.create(req, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }

    @PutMapping("/{id}")
    public ChangeRequestDto update(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateChangeRequest req,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   HttpServletRequest request) {
        return service.update(id, req, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }

    @PostMapping("/{id}/transition")
    public ChangeRequestDto transition(@PathVariable UUID id,
                                       @Valid @RequestBody TransitionRequest req,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       HttpServletRequest request) {
        return service.transition(id, req.getStatus(), req.getVersion(), userDetails.getUser(), (String) request.getAttribute("traceId"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       HttpServletRequest request) {
        service.delete(id, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }
}
