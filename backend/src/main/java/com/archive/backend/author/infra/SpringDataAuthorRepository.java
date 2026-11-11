package com.archive.backend.author.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataAuthorRepository extends JpaRepository<AuthorEntity, UUID> {

    /**
     * Loads the person for a change, locking its row until the transaction commits (docs/backend.md).
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthorEntity> findById(UUID id);
}
