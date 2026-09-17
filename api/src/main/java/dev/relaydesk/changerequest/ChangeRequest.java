package dev.relaydesk.changerequest;

import dev.relaydesk.common.BaseEntity;
import dev.relaydesk.user.Team;
import dev.relaydesk.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "change_requests")
public class ChangeRequest extends BaseEntity {

    @Column(nullable = false, unique = true, updatable = false)
    private String publicKey;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String riskLevel;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String rollbackPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Version
    @Column(nullable = false)
    private long version = 0;

    @Column(nullable = false)
    private int submissionRound = 0;

    private OffsetDateTime plannedWindowStart;
    private OffsetDateTime plannedWindowEnd;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Review> reviews;

    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Comment> comments;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        } else {
            updatedAt = OffsetDateTime.now();
        }
    }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRollbackPlan() { return rollbackPlan; }
    public void setRollbackPlan(String rollbackPlan) { this.rollbackPlan = rollbackPlan; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public int getSubmissionRound() { return submissionRound; }
    public void setSubmissionRound(int submissionRound) { this.submissionRound = submissionRound; }
    public OffsetDateTime getPlannedWindowStart() { return plannedWindowStart; }
    public void setPlannedWindowStart(OffsetDateTime plannedWindowStart) { this.plannedWindowStart = plannedWindowStart; }
    public OffsetDateTime getPlannedWindowEnd() { return plannedWindowEnd; }
    public void setPlannedWindowEnd(OffsetDateTime plannedWindowEnd) { this.plannedWindowEnd = plannedWindowEnd; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
    public java.util.List<Review> getReviews() { return reviews; }
    public void setReviews(java.util.List<Review> reviews) { this.reviews = reviews; }
    public java.util.List<Comment> getComments() { return comments; }
    public void setComments(java.util.List<Comment> comments) { this.comments = comments; }
}
