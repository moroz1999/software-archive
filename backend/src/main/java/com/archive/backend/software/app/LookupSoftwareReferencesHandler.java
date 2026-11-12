package com.archive.backend.software.app;

import com.archive.backend.software.read.SoftwareReferenceLookup;
import com.archive.backend.software.read.SoftwareReferenceOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Finds a small page of current people, groups and labels for a software form. */
@Service
@RequiredArgsConstructor
public class LookupSoftwareReferencesHandler {

    private static final int LIMIT = 30;

    private final SoftwareReferenceLookup lookup;

    @Transactional(readOnly = true)
    public List<SoftwareReferenceOption> handle(LookupSoftwareReferencesQuery query) {
        if (query.phrase().isEmpty() || query.types().isEmpty()) {
            return List.of();
        }
        return lookup.find(
                query.phrase().value(),
                query.types().stream().map(type -> type.code()).collect(java.util.stream.Collectors.toSet()),
                LIMIT);
    }
}
