package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseFileStructureRepository;
import com.archive.backend.software.domain.ReleaseFileStructuresOutdated;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** An outdated structure goes with the change that outdated it: until parsed again, the file waits for one. */
@Service
@RequiredArgsConstructor
public class DiscardOutdatedReleaseFileStructuresHandler {

    private final ReleaseFileStructureRepository structures;

    @EventListener
    public void on(ReleaseFileStructuresOutdated outdated) {
        outdated.releases().forEach(structures::discard);
    }
}
