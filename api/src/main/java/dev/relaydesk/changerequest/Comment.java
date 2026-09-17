package dev.relaydesk.changerequest;

import dev.relaydesk.common.BaseEntity;
import dev.relaydesk.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequest changeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    private OffsetDateTime deletedAt;

    public ChangeRequest getChangeRequest() { return changeRequest; }
    public void setChangeRequest(ChangeRequest changeRequest) { this.changeRequest = changeRequest; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Comment getParent() { return parent; }
    public void setParent(Comment parent) { this.parent = parent; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public OffsetDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(OffsetDateTime deletedAt) { this.deletedAt = deletedAt; }
}
