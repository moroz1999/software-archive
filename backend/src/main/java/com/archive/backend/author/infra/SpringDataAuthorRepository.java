package com.archive.backend.author.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAuthorRepository extends JpaRepository<AuthorEntity, UUID> {

    /**
     * Loads the person for a change, locking its row until the transaction commits (docs/backend.md).
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthorEntity> findById(UUID id);

    /** The person a label belongs to, locked for a change like {@link #findById}. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM AuthorEntity o JOIN o.labels l WHERE l.id = :label")
    Optional<AuthorEntity> findByLabel(@Param("label") UUID label);
}
