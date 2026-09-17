package dev.relaydesk.changerequest;

import dev.relaydesk.common.BaseEntity;
import dev.relaydesk.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequest changeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(nullable = false)
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private int submissionRound;

    public ChangeRequest getChangeRequest() { return changeRequest; }
    public void setChangeRequest(ChangeRequest changeRequest) { this.changeRequest = changeRequest; }
    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public int getSubmissionRound() { return submissionRound; }
    public void setSubmissionRound(int submissionRound) { this.submissionRound = submissionRound; }
}
