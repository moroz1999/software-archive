package com.archive.backend.importer.infra;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataImportRunRepository extends JpaRepository<ImportRunEntity, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ImportRunEntity> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ImportRunEntity> findByState(String state);
}
