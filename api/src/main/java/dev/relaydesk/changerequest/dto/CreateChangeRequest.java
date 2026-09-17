package dev.relaydesk.changerequest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public class CreateChangeRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String priority;
    @NotBlank
    private String riskLevel;
    @NotBlank
    private String service;
    @NotBlank
    private String environment;
    private OffsetDateTime scheduledStart;
    private OffsetDateTime scheduledEnd;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public OffsetDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(OffsetDateTime scheduledStart) { this.scheduledStart = scheduledStart; }
    public OffsetDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(OffsetDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }
}
