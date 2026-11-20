package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.domain.ScreenshotImages;
import com.archive.backend.software.read.ScreenshotFile;
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

/** The pictures follow the change they are stored or discarded in. */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@IntegrationTest
class FileSystemScreenshotFilesIT {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AY_MOD = UUID.fromString("00000000-0000-0000-0000-000000000012");

    @Autowired
    private FileSystemScreenshotFiles files;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactions;

    private ScreenshotId screenshot;

    @BeforeEach
    void elite() {
        jdbc.update("DELETE FROM software");
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update("INSERT INTO software_release (id, software_id, type) VALUES (?, ?, 'mod')", AY_MOD, ELITE);
        screenshot = new ScreenshotId(UUID.randomUUID());
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

    private void ofSoftware(ScreenshotId id, String format) {
        jdbc.update(
                "INSERT INTO software_screenshot (software_id, position, id, format, file_name, size) VALUES (?, 0, ?, ?, 'shot', 1)",
                ELITE,
                id.value(),
                format);
    }

    private Path stored(ScreenshotId id) {
        return files.find(id.value()).map(ScreenshotFile::path).orElseThrow();
    }

    @Test
    void storesThePictureByteForByteAndFindsItWithItsMediaType() throws IOException {
        inTransaction(() -> {
            ofSoftware(screenshot, "gif");
            files.store(screenshot, ScreenshotImages.image(ScreenshotImages.gif()));
        });

        ScreenshotFile file = files.find(screenshot.value()).orElseThrow();
        assertThat(file.mediaType()).isEqualTo("image/gif");
        assertThat(Files.readAllBytes(file.path())).isEqualTo(ScreenshotImages.gif());
    }

    @Test
    void findsThePictureOfAReleaseScreenshot() {
        inTransaction(() -> {
            jdbc.update(
                    "INSERT INTO software_release_screenshot (release_id, position, id, format, file_name, size) VALUES (?, 0, ?, 'webp', 'shot.webp', 1)",
                    AY_MOD,
                    screenshot.value());
            files.store(screenshot, ScreenshotImages.image(ScreenshotImages.webp()));
        });

        assertThat(files.find(screenshot.value()))
                .hasValueSatisfying(file -> assertThat(file.mediaType()).isEqualTo("image/webp"));
    }

    @Test
    void findsNothingForAnUnknownScreenshot() {
        assertThat(files.find(screenshot.value())).isEmpty();
    }

    @Test
    void deletesThePictureOfAChangeThatRollsBack() {
        Path[] where = new Path[1];
        rolledBack(() -> {
            ofSoftware(screenshot, "png");
            files.store(screenshot, ScreenshotImages.image(ScreenshotImages.png()));
            where[0] = stored(screenshot);
            assertThat(where[0]).exists();
        });

        assertThat(where[0]).doesNotExist();
    }

    @Test
    void deletesADiscardedPictureOnlyOnceTheChangeCommits() {
        inTransaction(() -> {
            ofSoftware(screenshot, "png");
            files.store(screenshot, ScreenshotImages.image(ScreenshotImages.png()));
        });
        Path picture = stored(screenshot);

        rolledBack(() -> files.discard(List.of(screenshot)));
        assertThat(picture).exists();

        inTransaction(() -> {
            files.discard(List.of(screenshot));
            assertThat(picture).exists();
        });
        assertThat(picture).doesNotExist();
    }
}
