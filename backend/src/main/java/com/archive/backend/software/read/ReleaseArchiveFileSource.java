package com.archive.backend.software.read;

import java.util.Optional;
import java.util.UUID;

public interface ReleaseArchiveFileSource {

    Optional<ReleaseArchiveFile> find(UUID softwareId, UUID releaseId);
}
