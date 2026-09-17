package dev.relaydesk.changerequest.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewRequest {
    @NotBlank
    private String status; // APPROVED, REJECTED
    private String notes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
