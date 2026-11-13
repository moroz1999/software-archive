package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseArchiveNotFoundException;
import com.archive.backend.software.read.ReleaseArchiveFile;
import com.archive.backend.software.read.ReleaseArchiveFileSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetReleaseArchiveFileHandler {

    private final ReleaseArchiveFileSource files;

    @Transactional(readOnly = true)
    public ReleaseArchiveFile handle(GetReleaseArchiveFileQuery query) {
        return files.find(query.softwareId().value(), query.releaseId().value())
                .orElseThrow(ReleaseArchiveNotFoundException::new);
    }
}
