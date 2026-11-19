package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseFileStructuresOutdated;
import com.archive.backend.software.domain.ReleaseId;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Parses release files once the change that stored them has committed: a file that cannot be parsed never undoes
 * the change, it only waits for its structure (docs/features/release-files.md).
 */
@Service
@RequiredArgsConstructor
public class ParseOutdatedReleaseFilesHandler {

    private static final Logger log = LoggerFactory.getLogger(ParseOutdatedReleaseFilesHandler.class);

    private final ReparseReleaseFileHandler reparse;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ReleaseFileStructuresOutdated outdated) {
        for (ReleaseId release : outdated.releases()) {
            try {
                reparse.handle(new ReparseReleaseFileCommand(release));
            } catch (RuntimeException e) {
                log.warn("Could not parse the file of release {}", release.value(), e);
            }
        }
    }
}
