package com.archive.backend.software.read;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Reads software as plain read models, never as domain entities (docs/backend.md).
 */
public interface SoftwareReadRepository {

    /**
     * A page of software; only of the platform when one is given.
     */
    Page<SoftwareSummary> findPage(@Nullable String platform, Pageable pageable);

    /**
     * The most recently added software first, only of the platform when one is given; the sort is ignored.
     */
    Page<SoftwareSummary> findLatest(@Nullable String platform, Pageable pageable);

    Optional<SoftwareDetails> findById(UUID id);

    /**
     * The releases of software, each with only its own hardware; none for unknown software.
     */
    List<SoftwareRelease> findReleases(UUID softwareId);

    /**
     * A release of the software as it states itself; empty when the software has no such release.
     */
    Optional<ReleaseDetails> findRelease(UUID softwareId, UUID releaseId);
}
