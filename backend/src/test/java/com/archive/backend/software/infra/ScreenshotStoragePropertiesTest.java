package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class ScreenshotStoragePropertiesTest {

    private static ScreenshotStorageProperties bound(String storagePath) {
        return new Binder(new MapConfigurationPropertySource(Map.of("screenshots.storage-path", storagePath)))
                .bind("screenshots", ScreenshotStorageProperties.class)
                .get();
    }

    @Test
    void takesAFolderAboveTheWorkingOneAsAFolderOfTheFileSystem() {
        assertThat(bound("../upload").folder())
                .isEqualTo(Path.of("").toAbsolutePath().resolve("../upload").normalize());
    }

    @Test
    void takesAnAbsoluteFolderAsItIs() {
        assertThat(bound("/srv/archive/upload").folder()).isEqualTo(Path.of("/srv/archive/upload"));
    }
}
