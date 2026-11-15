package com.archive.backend.label.infra;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLabelRepository extends JpaRepository<LabelEntity, UUID> {

    /** The label while it is of nobody, its row locked until the transaction ends (docs/backend.md). */
    @Query(
            value = "SELECT * FROM label WHERE id = :id AND person_id IS NULL AND group_id IS NULL FOR NO KEY UPDATE",
            nativeQuery = true)
    Optional<LabelEntity> lockOfNobody(@Param("id") UUID id);
}
