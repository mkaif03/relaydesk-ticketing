package dev.relaydesk.release.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReleaseItemDto {
    private UUID id;
    private UUID changeRequestId;
    private UUID releaseId;
    private UUID addedById;
    private boolean active;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChangeRequestId() { return changeRequestId; }
    public void setChangeRequestId(UUID changeRequestId) { this.changeRequestId = changeRequestId; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public UUID getAddedById() { return addedById; }
    public void setAddedById(UUID addedById) { this.addedById = addedById; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
