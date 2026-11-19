package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategorySets;
import com.archive.backend.software.domain.Screenshot;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.ScreenshotPictureRepository;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCreditSets;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds to software what an import found and it lacks, never rewriting what it has (docs/domain/imports.md):
 * categories and participants join those there, a screenshot of a size it has is not taken again.
 */
@Service
@RequiredArgsConstructor
public class SupplementSoftwareHandler {

    private final SoftwareRepository repository;
    private final CategorySets categorySets;
    private final SoftwareCreditSets creditSets;
    private final ScreenshotPictureRepository files;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(SupplementSoftwareCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        software.requireChangeableBy(c.actor());
        Set<CategoryId> categories = new HashSet<>(software.getCategories().ids());
        categories.addAll(c.categories());
        software.supplement(
                c.description(),
                c.releaseDate(),
                c.preservation(),
                categorySets.of(categories),
                creditSets.of(c.credits()));
        List<ScreenshotImage> images = software.screenshotsOfNewSizes(c.screenshots());
        List<Screenshot> added = images.stream()
                .map(image ->
                        new Screenshot(new ScreenshotId(UUID.randomUUID()), image.format(), image.name(), image.size()))
                .toList();
        if (!added.isEmpty()) {
            software.addScreenshots(null, added, c.actor());
            for (int i = 0; i < added.size(); i++) {
                files.store(added.get(i).id(), images.get(i));
            }
        }
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
    }
}
