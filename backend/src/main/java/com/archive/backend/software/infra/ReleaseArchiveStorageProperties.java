package com.archive.backend.software.infra;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where release archive files are kept outside the database. */
@ConfigurationProperties("release-archives")
public record ReleaseArchiveStorageProperties(String storagePath) {

    public Path folder() {
        return Path.of(storagePath).toAbsolutePath().normalize();
    }
}
