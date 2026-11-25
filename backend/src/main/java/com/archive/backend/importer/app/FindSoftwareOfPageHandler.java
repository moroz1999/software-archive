package com.archive.backend.importer.app;

import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.read.ExternalOriginReadRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The software a page of another site is a source of, if any. */
@Service
@RequiredArgsConstructor
public class FindSoftwareOfPageHandler {

    private final ExternalOriginReadRepository reads;

    @Transactional(readOnly = true)
    public Optional<UUID> handle(FindSoftwareOfPageQuery q) {
        ExternalOrigin origin = ExternalOrigin.softwarePage(q.page());
        return reads.findEntity(origin.archive(), origin.kind().name(), origin.externalId());
    }
}
