package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseArchivesForParsing;
import com.archive.backend.software.domain.ReleaseFilesReparsed;
import com.archive.backend.software.domain.ReleaseId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Explicitly rebuilds every stored release file tree; a file that fails is logged and the rest go on. */
@Service
@RequiredArgsConstructor
public class ReparseReleaseFilesHandler {

    private static final Logger log = LoggerFactory.getLogger(ReparseReleaseFilesHandler.class);

    private final ReleaseArchivesForParsing archives;
    private final ReparseReleaseFileHandler reparse;

    public ReleaseFilesReparsed handle() {
        int parsed = 0;
        int failed = 0;
        for (ReleaseId release : archives.findAllIds()) {
            try {
                reparse.handle(new ReparseReleaseFileCommand(release));
                parsed++;
            } catch (RuntimeException e) {
                log.warn("Could not parse the file of release {}", release.value(), e);
                failed++;
            }
        }
        return new ReleaseFilesReparsed(parsed, failed);
    }
}
