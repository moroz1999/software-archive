package com.archive.backend.software.app;

import com.archive.backend.software.read.SoftwareLink;
import com.archive.backend.software.read.SoftwareReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LookupSoftwareTitlesHandler {

    private static final int LIMIT = 30;

    private final SoftwareReadRepository read;

    @Transactional(readOnly = true)
    public List<SoftwareLink> handle(LookupSoftwareTitlesQuery query) {
        return query.phrase().isEmpty()
                ? List.of()
                : read.findTitles(query.phrase().value(), LIMIT);
    }
}
