package com.archive.backend.category.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    /**
     * Loads the category for a change, locking its row until the transaction commits (docs/backend.md).
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CategoryEntity> findById(UUID id);

    /**
     * The place after the last child of the parent; of the top of the tree without one.
     */
    @Query("SELECT coalesce(max(c.position) + 1, 0) FROM CategoryEntity c"
            + " WHERE (:parent IS NULL AND c.parentId IS NULL) OR c.parentId = :parent")
    int nextPosition(@Param("parent") @Nullable UUID parent);
}
