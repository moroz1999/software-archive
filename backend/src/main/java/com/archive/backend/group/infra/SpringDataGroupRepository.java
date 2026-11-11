package com.archive.backend.group.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataGroupRepository extends JpaRepository<GroupEntity, UUID> {

    /**
     * Loads the group for a change, locking its row until the transaction commits (docs/backend.md).
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GroupEntity> findById(UUID id);
}
