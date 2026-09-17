package dev.relaydesk.release;

import dev.relaydesk.common.BaseEntity;
import dev.relaydesk.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "releases")
public class Release extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    private OffsetDateTime shippedAt;
    private OffsetDateTime rolledBackAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Version
    @Column(nullable = false)
    private long version = 0;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        } else {
            updatedAt = OffsetDateTime.now();
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    public OffsetDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(OffsetDateTime shippedAt) { this.shippedAt = shippedAt; }
    public OffsetDateTime getRolledBackAt() { return rolledBackAt; }
    public void setRolledBackAt(OffsetDateTime rolledBackAt) { this.rolledBackAt = rolledBackAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
