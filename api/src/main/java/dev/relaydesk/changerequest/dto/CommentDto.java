package dev.relaydesk.changerequest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CommentDto {
    private UUID id;
    private UUID changeRequestId;
    private UUID authorId;
    private String content;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChangeRequestId() { return changeRequestId; }
    public void setChangeRequestId(UUID changeRequestId) { this.changeRequestId = changeRequestId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
