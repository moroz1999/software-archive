package com.archive.backend.software.domain;

import java.util.List;
import java.util.Optional;

/** Stored release archives available to the explicit reparsing command. */
public interface ReleaseArchivesForParsing {

    List<ReleaseId> findAllIds();

    Optional<ReleaseArchiveForParsing> find(ReleaseId release);
}
