package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.software.domain.ReleaseArchiveId;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.read.ReleaseArchiveFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@IntegrationTest
class FileSystemReleaseArchivesIT {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID RELEASE = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final byte[] CONTENT = "release archive".getBytes(java.nio.charset.StandardCharsets.UTF_8);

    @Autowired
    private FileSystemReleaseArchives files;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactions;

    private ReleaseArchiveId archive;

    @BeforeEach
    void elite() {
        jdbc.update("DELETE FROM software");
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update("INSERT INTO software_release (id, software_id, type) VALUES (?, ?, 'mod')", RELEASE, ELITE);
        archive = new ReleaseArchiveId(UUID.randomUUID());
    }

    @Test
    void storesAndFindsTheArchiveByteForByteUnderItsOriginalName() throws IOException {
        inTransaction(() -> {
            attach(archive);
            files.store(archive, upload());
        });

        ReleaseArchiveFile file = files.find(ELITE, RELEASE).orElseThrow();
        assertThat(file.fileName()).isEqualTo("elite.zip");
        assertThat(file.size()).isEqualTo(CONTENT.length);
        assertThat(Files.readAllBytes(file.path())).isEqualTo(CONTENT);
    }

    @Test
    void deletesAStoredArchiveWhenItsChangeRollsBack() {
        Path[] stored = new Path[1];
        rolledBack(() -> {
            attach(archive);
            files.store(archive, upload());
            stored[0] = files.find(ELITE, RELEASE).orElseThrow().path();
            assertThat(stored[0]).exists();
        });

        assertThat(stored[0]).doesNotExist();
    }

    @Test
    void deletesADiscardedArchiveOnlyAfterCommit() {
        inTransaction(() -> {
            attach(archive);
            files.store(archive, upload());
        });
        Path stored = files.find(ELITE, RELEASE).orElseThrow().path();

        rolledBack(() -> files.discard(List.of(archive)));
        assertThat(stored).exists();

        inTransaction(() -> files.discard(List.of(archive)));
        assertThat(stored).doesNotExist();
    }

    private ReleaseArchiveUpload upload() {
        return new ReleaseArchiveUpload("elite.zip", CONTENT.length, () -> new ByteArrayInputStream(CONTENT));
    }

    private void attach(ReleaseArchiveId id) {
        jdbc.update(
                "UPDATE software_release SET archive_id = ?, archive_file_name = 'elite.zip', archive_size = ? WHERE id = ?",
                id.value(),
                CONTENT.length,
                RELEASE);
    }

    private void inTransaction(Runnable work) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> work.run());
    }

    private void rolledBack(Runnable work) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            work.run();
            status.setRollbackOnly();
        });
    }
}
