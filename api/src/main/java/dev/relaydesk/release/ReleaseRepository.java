package dev.relaydesk.release;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReleaseRepository extends JpaRepository<Release, UUID> {
}
