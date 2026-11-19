package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseArchiveForParsing;
import com.archive.backend.software.domain.ReleaseArchivesForParsing;
import com.archive.backend.software.domain.ReleaseFileStructureParser;
import com.archive.backend.software.domain.ReleaseFileStructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Rebuilds the structure of one release file in a transaction of its own, apart from the change that stored it. */
@Service
@RequiredArgsConstructor
public class ReparseReleaseFileHandler {

    private final ReleaseArchivesForParsing archives;
    private final ReleaseFileStructureParser parser;
    private final ReleaseFileStructureRepository structures;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(ReparseReleaseFileCommand c) {
        ReleaseArchiveForParsing archive = archives.find(c.releaseId()).orElse(null);
        if (archive == null) {
            return;
        }
        structures.replace(c.releaseId(), parser.parse(archive.platform(), archive.upload()));
    }
}
