package com.archive.backend.software.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SoftwareJpaRepository extends JpaRepository<SoftwareEntity, UUID> {

    /** Software is loaded only to change it: its row stays locked until the change commits (docs/backend.md). */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SoftwareEntity> findById(UUID id);
}
