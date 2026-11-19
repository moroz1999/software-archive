package com.archive.backend.software.app;

import com.archive.backend.software.domain.Screenshot;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotPictureRepository;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddScreenshotsHandler {

    private final SoftwareRepository repository;
    private final ScreenshotPictureRepository files;
    private final ApplicationEventPublisher events;

    @Transactional
    public List<ScreenshotId> handle(AddScreenshotsCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        List<Screenshot> added = c.images().stream()
                .map(image ->
                        new Screenshot(new ScreenshotId(UUID.randomUUID()), image.format(), image.name(), image.size()))
                .toList();
        // Checked before anything is stored: no picture is kept for screenshots that are refused.
        software.addScreenshots(c.releaseId(), added, c.actor());
        for (int i = 0; i < added.size(); i++) {
            files.store(added.get(i).id(), c.images().get(i));
        }
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
        return added.stream().map(Screenshot::id).toList();
    }
}
