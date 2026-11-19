package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseArchiveNotFoundException;
import com.archive.backend.software.domain.ReleaseNotFoundException;
import com.archive.backend.software.read.ReleaseFileReadRepository;
import com.archive.backend.software.read.ReleaseFiles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetReleaseFilesHandler {

    private final ReleaseFileReadRepository files;

    @Transactional(readOnly = true)
    public ReleaseFiles handle(GetReleaseFilesQuery q) {
        ReleaseFiles found =
                files.find(q.softwareId().value(), q.releaseId().value()).orElseThrow(ReleaseNotFoundException::new);
        if (!found.archive()) {
            throw new ReleaseArchiveNotFoundException();
        }
        return found;
    }
}
