package com.archive.backend.remote.app;

import com.archive.backend.remote.read.DownloadedFile;
import com.archive.backend.remote.read.RemoteResources;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A file of another site, downloaded within its limit to be stored. */
@Service
@RequiredArgsConstructor
public class DownloadFileHandler {

    private final RemoteResources remote;

    @Transactional(readOnly = true)
    public DownloadedFile handle(DownloadFileQuery q) {
        return remote.download(q.url().value(), q.limit().value());
    }
}
