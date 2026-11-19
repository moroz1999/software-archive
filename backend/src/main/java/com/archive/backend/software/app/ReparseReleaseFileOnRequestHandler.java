package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the structure of a release file again when whoever may change the release asks for it; the parse runs in
 * the request, so its outcome is seen at once.
 */
@Service
@RequiredArgsConstructor
public class ReparseReleaseFileOnRequestHandler {

    private final SoftwareRepository repository;
    private final ReparseReleaseFileHandler reparse;

    @Transactional
    public void handle(ReparseReleaseFileOnRequestCommand c) {
        repository
                .findById(c.softwareId())
                .orElseThrow(SoftwareNotFoundException::new)
                .requireReleaseFileReparsableBy(c.releaseId(), c.actor());
        reparse.handle(new ReparseReleaseFileCommand(c.releaseId()));
    }
}
