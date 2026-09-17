package dev.relaydesk.release;

import dev.relaydesk.common.BaseEntity;
import dev.relaydesk.changerequest.ChangeRequest;
import dev.relaydesk.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "release_items")
public class ReleaseItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequest changeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    private User addedBy;

    @Column(nullable = false)
    private boolean active = true;

    public Release getRelease() { return release; }
    public void setRelease(Release release) { this.release = release; }
    public ChangeRequest getChangeRequest() { return changeRequest; }
    public void setChangeRequest(ChangeRequest changeRequest) { this.changeRequest = changeRequest; }
    public User getAddedBy() { return addedBy; }
    public void setAddedBy(User addedBy) { this.addedBy = addedBy; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
