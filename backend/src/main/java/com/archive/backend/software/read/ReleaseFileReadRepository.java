package com.archive.backend.software.read;

import java.util.Optional;
import java.util.UUID;

public interface ReleaseFileReadRepository {

    /** The files of a release of the software; nothing for an unknown software or release. */
    Optional<ReleaseFiles> find(UUID softwareId, UUID releaseId);
}
