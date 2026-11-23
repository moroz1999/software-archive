package com.archive.backend.hardware.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataHardwareRepository extends JpaRepository<HardwareEntity, String> {

    /** Loads the row for a change, locking it until the transaction commits (docs/backend.md). */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<HardwareEntity> findById(String code);
}
