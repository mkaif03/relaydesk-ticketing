package dev.relaydesk.release.dto;

import jakarta.validation.constraints.NotBlank;

public class ReleaseRequest {
    @NotBlank
    private String name;
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
