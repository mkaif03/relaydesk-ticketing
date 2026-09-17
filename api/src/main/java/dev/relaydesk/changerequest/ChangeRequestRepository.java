package dev.relaydesk.changerequest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, UUID> {
}
