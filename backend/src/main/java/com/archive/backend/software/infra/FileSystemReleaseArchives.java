package com.archive.backend.software.infra;

import com.archive.backend.software.domain.ReleaseArchiveId;
import com.archive.backend.software.domain.ReleaseArchiveRepository;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.read.ReleaseArchiveFile;
import com.archive.backend.software.read.ReleaseArchiveFileSource;
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

/** Release archives stored under internal UUIDs, following their database transactions. */
@Repository
public class FileSystemReleaseArchives implements ReleaseArchiveRepository, ReleaseArchiveFileSource {

    private final Path root;
    private final JdbcClient jdbc;

    public FileSystemReleaseArchives(ReleaseArchiveStorageProperties properties, JdbcClient jdbc) {
        this.root = properties.folder();
        this.jdbc = jdbc;
    }

    @Override
    public void store(ReleaseArchiveId id, ReleaseArchiveUpload upload) {
        Path file = file(id.value());
        try {
            Files.createDirectories(root);
            Path part = Files.createTempFile(root, id.value().toString(), ".part");
            try (InputStream in = upload.content().open()) {
                Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Files.deleteIfExists(part);
                throw e;
            }
            Files.move(part, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store release archive " + id.value(), e);
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
    public void discard(List<ReleaseArchiveId> ids) {
        List<Path> discarded = ids.stream().map(id -> file(id.value())).toList();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    discarded.forEach(FileSystemReleaseArchives::delete);
                }
            });
        } else {
            discarded.forEach(FileSystemReleaseArchives::delete);
        }
    }

    @Override
    public Optional<ReleaseArchiveFile> find(UUID softwareId, UUID releaseId) {
        return jdbc.sql("""
                        SELECT archive_id, archive_file_name, archive_size
                        FROM software_release
                        WHERE software_id = :softwareId AND id = :releaseId AND archive_id IS NOT NULL
                        """)
                .param("softwareId", softwareId)
                .param("releaseId", releaseId)
                .query((rs, row) -> new ReleaseArchiveFile(
                        file(rs.getObject("archive_id", UUID.class)),
                        rs.getString("archive_file_name"),
                        rs.getLong("archive_size")))
                .optional()
                .filter(found -> Files.isRegularFile(found.path()));
    }

    private Path file(UUID id) {
        return root.resolve(id.toString());
    }

    private static void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete release archive " + file, e);
        }
    }
}
