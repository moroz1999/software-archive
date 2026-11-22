package com.archive.backend.geo.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataCountryRepository extends JpaRepository<CountryEntity, UUID> {

    /** Loads the row for a change, locking it until the transaction commits (docs/backend.md). */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CountryEntity> findById(UUID id);
}
