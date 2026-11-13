package com.archive.backend.software.infra;

import com.archive.backend.software.domain.ScreenshotFormat;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.ScreenshotPictureRepository;
import com.archive.backend.software.read.ScreenshotFile;
import com.archive.backend.software.read.ScreenshotFileSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * The pictures of screenshots as files in the storage folder, each named by its screenshot. The files follow the
 * change that keeps the screenshots: a picture stored by a change that rolls back is deleted, and a discarded one is
 * deleted only once its change commits (docs/features/screenshots.md#storage).
 */
@Repository
public class FileSystemScreenshotFiles implements ScreenshotPictureRepository, ScreenshotFileSource {

    private final Path root;
    private final JdbcClient jdbc;

    public FileSystemScreenshotFiles(ScreenshotStorageProperties properties, JdbcClient jdbc) {
        this.root = properties.folder();
        this.jdbc = jdbc;
    }

    @Override
    public void store(ScreenshotId id, ScreenshotImage image) {
        Path file = file(id.value());
        try {
            Files.createDirectories(root);
            // Written aside and moved in, so a picture is either whole or absent.
            Path part = Files.createTempFile(root, id.value().toString(), ".part");
            try (InputStream in = image.content().open()) {
                Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Files.deleteIfExists(part);
                throw e;
            }
            Files.move(part, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store the picture of screenshot " + id.value(), e);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        delete(file);
                    }
                }
            });
        }
    }

    @Override
    public void discard(List<ScreenshotId> ids) {
        List<Path> discarded = ids.stream().map(id -> file(id.value())).toList();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    discarded.forEach(FileSystemScreenshotFiles::delete);
                }
            });
        } else {
            discarded.forEach(FileSystemScreenshotFiles::delete);
        }
    }

    @Override
    public Optional<ScreenshotFile> find(UUID screenshotId) {
        return jdbc.sql("""
                        SELECT format FROM software_screenshot WHERE id = :id
                        UNION ALL
                        SELECT format FROM software_release_screenshot WHERE id = :id
                        """)
                .param("id", screenshotId)
                .query(String.class)
                .optional()
                .map(format -> new ScreenshotFile(file(screenshotId), mediaType(ScreenshotFormat.fromCode(format))))
                .filter(found -> Files.isRegularFile(found.path()));
    }

    private Path file(UUID screenshotId) {
        return root.resolve(screenshotId.toString());
    }

    private static String mediaType(ScreenshotFormat format) {
        return "image/" + format.code();
    }

    private static void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete the picture " + file, e);
        }
    }
}
