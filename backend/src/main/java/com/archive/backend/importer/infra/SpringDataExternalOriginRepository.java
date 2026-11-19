package com.archive.backend.importer.infra;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataExternalOriginRepository extends JpaRepository<ExternalOriginEntity, ExternalOriginKey> {

    boolean existsByKeyOriginAndKeyKindAndEntityId(String origin, String kind, UUID entityId);

    @Modifying
    @Query("DELETE FROM ExternalOriginEntity o WHERE o.key.kind = :kind AND o.entityId = :entityId")
    void deleteByKeyKindAndEntityId(@Param("kind") String kind, @Param("entityId") UUID entityId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value =
                    "UPDATE external_origin SET kind = :toKind, entity_id = :to WHERE kind = :kind AND entity_id = :entityId",
            nativeQuery = true)
    void transfer(
            @Param("kind") String kind,
            @Param("entityId") UUID entityId,
            @Param("toKind") String toKind,
            @Param("to") UUID to);
}
