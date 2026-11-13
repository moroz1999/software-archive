package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseArchiveRepository;
import com.archive.backend.software.domain.ReleaseArchivesDiscarded;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscardReleaseArchiveFilesHandler {

    private final ReleaseArchiveRepository files;

    @EventListener
    public void on(ReleaseArchivesDiscarded discarded) {
        files.discard(discarded.archives());
    }
}
