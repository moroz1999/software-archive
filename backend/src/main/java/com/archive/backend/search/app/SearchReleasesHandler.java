package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.FoundRelease;
import com.archive.backend.search.read.ReleaseSearch;
import com.archive.backend.search.read.ReleaseSearchFilter;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReferenceId;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Finds releases apart from their software (docs/domain/ai-assistant.md#release-search). */
@Service
@RequiredArgsConstructor
public class SearchReleasesHandler {

    private final ReleaseSearch search;

    @Transactional(readOnly = true)
    public Page<FoundRelease> handle(SearchReleasesQuery q) {
        return search.find(
                new ReleaseSearchFilter(
                        text(q.word()),
                        q.software().stream().map(SoftwareId::value).collect(Collectors.toSet()),
                        q.types().stream().map(ReleaseType::code).collect(Collectors.toSet()),
                        year(q.yearFrom()),
                        year(q.yearTo()),
                        q.platform() == null ? null : q.platform().value(),
                        q.hardware().stream().map(HardwareCode::value).collect(Collectors.toSet()),
                        q.credited().stream().map(SoftwareReferenceId::value).collect(Collectors.toSet()),
                        q.roles(),
                        text(q.fileName()),
                        q.md5() == null ? null : q.md5().value()),
                q.pageable());
    }

    private static @Nullable String text(@Nullable CatalogKeyword keyword) {
        return keyword == null ? null : keyword.value();
    }

    private static @Nullable Integer year(@Nullable ReleaseYear year) {
        return year == null ? null : year.value();
    }
}
