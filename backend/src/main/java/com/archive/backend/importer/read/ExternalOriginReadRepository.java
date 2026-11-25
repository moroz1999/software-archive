package com.archive.backend.importer.read;

import java.util.Optional;
import java.util.UUID;

/** Which entry of the archive came from where (docs/domain/imports.md). */
public interface ExternalOriginReadRepository {

    /** The entry of the kind that has the id in the source archive, if any. */
    Optional<UUID> findEntity(String origin, String kind, String externalId);
}
