package com.archive.backend.remote.read;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A file downloaded from another site into a temporary file, deleted once closed: where it lies, the name the site
 * gives it ({@code Content-Disposition}, else the last part of the address), its size and its Content-Type.
 */
public record DownloadedFile(Path path, String fileName, long size, String contentType) implements AutoCloseable {

    @Override
    public void close() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
