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
 * Puts the pictures of the fixture screenshots into the storage, as the fixture data set needs them: a picture there
 * already is left as it is (docs/testing.md#fixtures).
 */
@Component
@Profile("fixtures")
public class FixtureScreenshotPictures {

    private final Path root;

    public FixtureScreenshotPictures(ScreenshotStorageProperties properties) {
        this.root = properties.folder();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void copy() throws IOException {
        Files.createDirectories(root);
        for (Resource picture :
                new PathMatchingResourcePatternResolver().getResources("classpath:db/fixtures/screenshots/*.png")) {
            String name = Objects.requireNonNull(picture.getFilename());
            Path file = root.resolve(name.substring(0, name.lastIndexOf('.')));
            if (Files.notExists(file)) {
                try (InputStream in = picture.getInputStream()) {
                    Files.copy(in, file);
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not copy the fixture picture " + name, e);
                }
            }
        }
    }
}
