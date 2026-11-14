package com.archive.backend.importer.infra;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataExternalOriginRepository extends JpaRepository<ExternalOriginEntity, ExternalOriginKey> {

    @Modifying
    @Query("DELETE FROM ExternalOriginEntity o WHERE o.key.kind = :kind AND o.entityId = :entityId")
    void deleteByKeyKindAndEntityId(@Param("kind") String kind, @Param("entityId") UUID entityId);
}
