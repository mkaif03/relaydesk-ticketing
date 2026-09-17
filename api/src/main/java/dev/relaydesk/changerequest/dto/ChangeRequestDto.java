package dev.relaydesk.changerequest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ChangeRequestDto {
    private UUID id;
    private String publicKey;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String riskLevel;
    private String service;
    private String environment;
    private UUID authorId;
    private UUID teamId;
    private OffsetDateTime scheduledStart;
    private OffsetDateTime scheduledEnd;
    private long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }
    public OffsetDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(OffsetDateTime scheduledStart) { this.scheduledStart = scheduledStart; }
    public OffsetDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(OffsetDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
