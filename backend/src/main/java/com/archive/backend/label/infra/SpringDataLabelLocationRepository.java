package com.archive.backend.label.infra;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataLabelLocationRepository extends JpaRepository<LabelLocationEntity, UUID> {

    @Query("SELECT l FROM LabelLocationEntity l WHERE l.entityType = 'LABEL' AND l.labelId = :label")
    List<LabelLocationEntity> findOfLabel(@Param("label") UUID label);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("DELETE FROM LabelLocationEntity l WHERE l.entityType = 'LABEL' AND l.labelId = :label")
    void deleteOfLabel(@Param("label") UUID label);
}
