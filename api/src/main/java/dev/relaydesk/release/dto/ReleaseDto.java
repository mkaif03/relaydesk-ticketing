package dev.relaydesk.release.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReleaseDto {
    private UUID id;
    private String name;
    private String status;
    private UUID managerId;
    private OffsetDateTime shippedAt;
    private OffsetDateTime rolledBackAt;
    private String notes;
    private long version;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }
    public OffsetDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(OffsetDateTime shippedAt) { this.shippedAt = shippedAt; }
    public OffsetDateTime getRolledBackAt() { return rolledBackAt; }
    public void setRolledBackAt(OffsetDateTime rolledBackAt) { this.rolledBackAt = rolledBackAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
