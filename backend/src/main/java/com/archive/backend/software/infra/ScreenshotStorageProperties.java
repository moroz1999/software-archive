package com.archive.backend.software.infra;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the pictures of screenshots are kept.
 *
 * @param storagePath the folder of the pictures, outside any container: they are kept as the database is
 */
@ConfigurationProperties("screenshots")
public record ScreenshotStorageProperties(String storagePath) {

    /**
     * The folder in the file system, relative ones from the working folder. Bound as text: Spring would read a path
     * as a resource of the class path, where a folder above the working one has no meaning.
     */
    public Path folder() {
        return Path.of(storagePath).toAbsolutePath().normalize();
    }
}
