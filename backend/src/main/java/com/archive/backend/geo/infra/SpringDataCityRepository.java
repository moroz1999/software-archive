package com.archive.backend.geo.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataCityRepository extends JpaRepository<CityEntity, UUID> {

    /** Loads the row for a change, locking it until the transaction commits (docs/backend.md). */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CityEntity> findById(UUID id);
}
