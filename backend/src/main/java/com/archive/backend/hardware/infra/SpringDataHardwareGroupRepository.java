package com.archive.backend.hardware.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SpringDataHardwareGroupRepository extends JpaRepository<HardwareGroupEntity, String> {

    /** Loads the row for a change, locking it until the transaction commits (docs/backend.md). */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<HardwareGroupEntity> findById(String code);

    /** The place after the last group. */
    @Query("SELECT COALESCE(MAX(g.position), 0) + 1 FROM HardwareGroupEntity g")
    int nextPosition();
}
