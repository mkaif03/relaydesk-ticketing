package dev.relaydesk.release;

import dev.relaydesk.release.dto.ReleaseDto;
import dev.relaydesk.release.dto.ReleaseItemDto;
import dev.relaydesk.release.dto.ReleaseItemRequest;
import dev.relaydesk.release.dto.ReleaseRequest;
import dev.relaydesk.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/releases")
@PreAuthorize("hasAnyRole('ADMIN', 'RELEASE_MANAGER')")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @GetMapping
    public List<ReleaseDto> listReleases() {
        return releaseService.listReleases();
    }

    @GetMapping("/{id}")
    public ReleaseDto getRelease(@PathVariable UUID id) {
        return releaseService.getRelease(id);
    }

    @PostMapping
    public ReleaseDto createRelease(@Valid @RequestBody ReleaseRequest req,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    HttpServletRequest request) {
        return releaseService.createRelease(req, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }

    @GetMapping("/{id}/items")
    public List<ReleaseItemDto> listItems(@PathVariable UUID id) {
        return releaseService.listItems(id);
    }

    @PostMapping("/{id}/items")
    public ReleaseItemDto addChangeRequest(@PathVariable UUID id,
                                           @Valid @RequestBody ReleaseItemRequest req,
                                           @AuthenticationPrincipal CustomUserDetails userDetails,
                                           HttpServletRequest request) {
        return releaseService.addChangeRequest(id, req, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }

    @PostMapping("/{id}/ship")
    public ReleaseDto shipRelease(@PathVariable UUID id,
                                  @RequestParam long version,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  HttpServletRequest request) {
        return releaseService.shipRelease(id, version, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }

    @PostMapping("/{id}/rollback")
    public ReleaseDto rollbackRelease(@PathVariable UUID id,
                                      @RequestParam long version,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      HttpServletRequest request) {
        return releaseService.rollbackRelease(id, version, userDetails.getUser(), (String) request.getAttribute("traceId"));
    }
}
