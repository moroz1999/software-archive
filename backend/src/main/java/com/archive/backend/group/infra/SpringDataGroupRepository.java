package com.archive.backend.group.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataGroupRepository extends JpaRepository<GroupEntity, UUID> {

    /**
     * Loads the group for a change, locking its row until the transaction commits (docs/backend.md).
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GroupEntity> findById(UUID id);

    /** The group a label belongs to, locked for a change like {@link #findById}. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM GroupEntity o JOIN o.labels l WHERE l.id = :label")
    Optional<GroupEntity> findByLabel(@Param("label") UUID label);
}
