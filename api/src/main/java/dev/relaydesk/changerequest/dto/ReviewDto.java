package dev.relaydesk.changerequest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReviewDto {
    private UUID id;
    private UUID changeRequestId;
    private UUID reviewerId;
    private String status;
    private String notes;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChangeRequestId() { return changeRequestId; }
    public void setChangeRequestId(UUID changeRequestId) { this.changeRequestId = changeRequestId; }
    public UUID getReviewerId() { return reviewerId; }
    public void setReviewerId(UUID reviewerId) { this.reviewerId = reviewerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
