package com.archive.backend.software.app;

import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReorderScreenshotsHandler {

    private final SoftwareRepository repository;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(ReorderScreenshotsCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        ReleaseId releaseId = c.releaseId();
        if (releaseId == null) {
            software.reorderPictures(c.gallery(), c.order(), c.actor());
        } else {
            software.reorderScreenshots(releaseId, c.order(), c.actor());
        }
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
    }
}
