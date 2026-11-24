package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReferenceId;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

/**
 * Releases found apart from their software (docs/domain/ai-assistant.md#release-search). Every condition given
 * narrows the result; within a set any of its values is enough.
 */
public record SearchReleasesQuery(
        @Nullable CatalogKeyword word,
        Set<SoftwareId> software,
        Set<ReleaseType> types,
        @Nullable ReleaseYear yearFrom,
        @Nullable ReleaseYear yearTo,
        @Nullable PlatformCode platform,
        Set<HardwareCode> hardware,
        Set<SoftwareReferenceId> credited,
        Set<String> roles,
        @Nullable CatalogKeyword fileName,
        @Nullable ReleaseFileHash md5,
        Pageable pageable) {

    public SearchReleasesQuery {
        software = Set.copyOf(software);
        types = Set.copyOf(types);
        hardware = Set.copyOf(hardware);
        credited = Set.copyOf(credited);
        roles = Set.copyOf(roles);
    }
}
