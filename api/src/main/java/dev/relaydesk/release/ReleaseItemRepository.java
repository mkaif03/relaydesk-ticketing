package dev.relaydesk.release;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReleaseItemRepository extends JpaRepository<ReleaseItem, UUID> {
    List<ReleaseItem> findByRelease(Release release);
}
