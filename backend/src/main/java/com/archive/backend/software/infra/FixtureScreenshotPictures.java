package com.archive.backend.software.infra;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Puts fixture screenshots and release archives into their storage folders; a file already there is left as it is
 * (docs/testing.md#fixtures).
 */
@Component
@Profile("fixtures")
public class FixtureScreenshotPictures {

    private final Path screenshotRoot;
    private final Path archiveRoot;

    public FixtureScreenshotPictures(
            ScreenshotStorageProperties screenshotProperties, ReleaseArchiveStorageProperties archiveProperties) {
        this.screenshotRoot = screenshotProperties.folder();
        this.archiveRoot = archiveProperties.folder();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void copy() throws IOException {
        Files.createDirectories(screenshotRoot);
        for (Resource picture :
                new PathMatchingResourcePatternResolver().getResources("classpath:db/fixtures/screenshots/*.png")) {
            String name = Objects.requireNonNull(picture.getFilename());
            copy(picture, screenshotRoot.resolve(name.substring(0, name.lastIndexOf('.'))));
        }
        Files.createDirectories(archiveRoot);
        for (Resource archive :
                new PathMatchingResourcePatternResolver().getResources("classpath:db/fixtures/release-archives/*")) {
            copy(archive, archiveRoot.resolve(Objects.requireNonNull(archive.getFilename())));
        }
    }

    private static void copy(Resource source, Path target) {
        if (Files.notExists(target)) {
            try (InputStream in = source.getInputStream()) {
                Files.copy(in, target);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not copy the fixture file " + source.getFilename(), e);
            }
        }
    }
}
