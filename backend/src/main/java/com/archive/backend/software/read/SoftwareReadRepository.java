package com.archive.backend.software.read;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/** Reads software as plain read models, never as domain entities (docs/backend.md). */
public interface SoftwareReadRepository {

    Page<SoftwareSummary> findPage(Pageable pageable);

    Optional<SoftwareDetails> findById(UUID id);
}
