package dev.relaydesk.release.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ReleaseItemRequest {
    @NotNull
    private UUID changeRequestId;

    public UUID getChangeRequestId() { return changeRequestId; }
    public void setChangeRequestId(UUID changeRequestId) { this.changeRequestId = changeRequestId; }
}
