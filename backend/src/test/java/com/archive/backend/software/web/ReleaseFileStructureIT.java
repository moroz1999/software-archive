package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.ReparseReleaseFilesHandler;
import com.archive.backend.software.app.UpdateReleaseCommand;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.domain.ReleaseArchiveRemoval;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseFileStructureParser;
import com.archive.backend.software.domain.ReleaseFilesReparsed;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.ReleaseArchiveFileSource;
import com.archive.backend.user.TestActors;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Release files are parsed after the change that stored them commits, so every change here commits for real.
 */
@SpringBootTest(properties = {"release-files.max-expanded-bytes=1024", "release-files.max-container-depth=2"})
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class ReleaseFileStructureIT {

    private static final SoftwareId SOFTWARE = new SoftwareId(UUID.fromString("00000000-0000-0000-00f1-000000000001"));
    private static final byte[] ROM = "vector program".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private CreateReleaseHandler createRelease;

    @Autowired
    private UpdateReleaseHandler updateRelease;

    @Autowired
    private UpdateSoftwareHandler updateSoftware;

    @Autowired
    private ReparseReleaseFilesHandler reparse;

    @Autowired
    private ReleaseArchiveFileSource archives;

    @MockitoSpyBean
    private ReleaseFileStructureParser parser;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void software() {
        TestActors.store(jdbc);
        removeSoftware();
        jdbc.update(
                "INSERT INTO software (id, title, platform_code, added_by) VALUES (?, 'Elite', 'vector', ?)",
                SOFTWARE.value(),
                TestActors.ADMIN_ID.value());
    }

    @AfterEach
    void removeSoftware() {
        jdbc.update("DELETE FROM software WHERE id = ?", SOFTWARE.value());
    }

    @Test
    void parsesNestedZipFilesBySignatureAndStoresTheirHashes() throws Exception {
        byte[] nested = zip(Map.of("NEXT.rom", "next program".getBytes(StandardCharsets.UTF_8)));
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("docs/readme.txt", "Read me".getBytes(StandardCharsets.UTF_8));
        files.put("games/ELITE.ROM", ROM);
        files.put("games/parts.bin", nested);
        byte[] archive = zip(files);

        ReleaseId release = create("bundle.bin", archive);

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("bundle.bin"))
                .andExpect(jsonPath("$.root.directory").value(false))
                .andExpect(jsonPath("$.root.executable").value(false))
                .andExpect(jsonPath("$.root.children[0].name").value("docs"))
                .andExpect(jsonPath("$.root.children[0].directory").value(true))
                .andExpect(jsonPath("$.root.children[0].children[0].name").value("readme.txt"))
                .andExpect(jsonPath("$.root.children[1].name").value("games"))
                .andExpect(jsonPath("$.root.children[1].children[0].name").value("ELITE.ROM"))
                .andExpect(jsonPath("$.root.children[1].children[0].executable").value(true))
                .andExpect(jsonPath("$.root.children[1].children[1].name").value("parts.bin"))
                .andExpect(jsonPath("$.root.children[1].children[1].children[0].name")
                        .value("NEXT.rom"))
                .andExpect(jsonPath("$.root.children[1].children[1].children[0].executable")
                        .value(true));

        Map<String, FileRow> rows = jdbc
                .query(
                        "SELECT file_name, md5, directory, executable FROM release_file WHERE release_id = ?",
                        (rs, row) -> new FileRow(
                                rs.getString("file_name"),
                                rs.getString("md5"),
                                rs.getBoolean("directory"),
                                rs.getBoolean("executable")),
                        release.value())
                .stream()
                .collect(Collectors.toMap(FileRow::name, row -> row));
        assertThat(rows)
                .containsEntry("bundle.bin", new FileRow("bundle.bin", md5(archive), false, false))
                .containsEntry("docs", new FileRow("docs", null, true, false))
                .containsEntry("ELITE.ROM", new FileRow("ELITE.ROM", md5(ROM), false, true))
                .containsEntry("parts.bin", new FileRow("parts.bin", md5(nested), false, false));
    }

    @Test
    void neverParsesAnExecutableRomEvenWhenItsContentIsAZip() throws Exception {
        byte[] archive = zip(Map.of("hidden.txt", "hidden".getBytes(StandardCharsets.UTF_8)));
        ReleaseId release = create("ELITE.ROM", archive);

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("ELITE.ROM"))
                .andExpect(jsonPath("$.root.executable").value(true))
                .andExpect(jsonPath("$.root.children").isEmpty());
    }

    @Test
    void keepsAnUnreadableZipAsTheRootFile() throws Exception {
        ReleaseId release = create("broken.bin", "PK-not-a-zip".getBytes(StandardCharsets.UTF_8));

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("broken.bin"))
                .andExpect(jsonPath("$.root.children").isEmpty());
    }

    @Test
    void keepsAZipWithNamesOutsideUtf8AsTheRootFile() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, Charset.forName("CP866"))) {
            zip.putNextEntry(new ZipEntry("ИГРА.ROM"));
            zip.write(ROM);
            zip.closeEntry();
        }
        ReleaseId release = create("igra.zip", bytes.toByteArray());

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("igra.zip"))
                .andExpect(jsonPath("$.root.children").isEmpty());
    }

    @Test
    void stopsOpeningAContainerBeyondTheExpandedSizeLimit() throws Exception {
        ReleaseId release = create("large.zip", zip(Map.of("large.bin", new byte[1025])));

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("large.zip"))
                .andExpect(jsonPath("$.root.children").isEmpty());
    }

    @Test
    void stopsOpeningNestedContainersAtTheDepthLimit() throws Exception {
        byte[] third = zip(Map.of("program.rom", ROM));
        byte[] second = zip(Map.of("third.zip", third));
        byte[] first = zip(Map.of("second.zip", second));
        ReleaseId release = create("first.zip", first);

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.children[0].name").value("second.zip"))
                .andExpect(jsonPath("$.root.children[0].children[0].name").value("third.zip"))
                .andExpect(jsonPath("$.root.children[0].children[0].children").isEmpty());
    }

    @Test
    void keepsTheReleaseWhenItsFileCannotBeParsedAndShowsItAsWaiting() throws Exception {
        doThrow(new IllegalStateException("parser broke")).when(parser).parse(any(), any());

        ReleaseId release = create("elite.zip", zip(Map.of("ELITE.ROM", ROM)));

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_release WHERE id = ?", Long.class, release.value()))
                .isOne();
        files(release).andExpect(status().isOk()).andExpect(jsonPath("$.root").value((Object) null));
    }

    @Test
    void showsAReleaseFileWithoutAParsedTreeAsWaiting() throws Exception {
        ReleaseId release = create("elite.zip", zip(Map.of("ELITE.ROM", ROM)));
        jdbc.update("DELETE FROM release_file WHERE release_id = ?", release.value());

        files(release).andExpect(status().isOk()).andExpect(jsonPath("$.root").value((Object) null));
    }

    @Test
    void answersThatAReleaseWithoutAFileHasNoTree() throws Exception {
        ReleaseId release = createRelease.handle(new CreateReleaseCommand(ADMIN, SOFTWARE, content(), Set.of(), null));

        files(release)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("release.archive-not-found"));
    }

    @Test
    void answersThatAReleaseOfAnotherSoftwareIsNotFound() throws Exception {
        ReleaseId release = create("elite.zip", zip(Map.of("ELITE.ROM", ROM)));

        mvc.perform(get("/api/software/{softwareId}/releases/{releaseId}/files", UUID.randomUUID(), release.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("release.not-found"));
    }

    @Test
    void reparsesEveryExistingReleaseFileOnDemandPastTheOnesThatFail() throws Exception {
        ReleaseId lost = create("lost.zip", zip(Map.of("LOST.ROM", ROM)));
        ReleaseId kept = create("elite.zip", zip(Map.of("ELITE.ROM", ROM)));
        Files.delete(archives.find(SOFTWARE.value(), lost.value()).orElseThrow().path());
        jdbc.update("DELETE FROM release_file WHERE release_id IN (?, ?)", lost.value(), kept.value());

        ReleaseFilesReparsed result = reparse.handle();

        assertThat(result.failed()).isOne();
        files(kept)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.children[0].name").value("ELITE.ROM"))
                .andExpect(jsonPath("$.root.children[0].executable").value(true));
        files(lost).andExpect(status().isOk()).andExpect(jsonPath("$.root").value((Object) null));
    }

    @Test
    void reparsesTheFilesOfTheReleasesWhenTheSoftwareMovesToAnotherPlatform() throws Exception {
        ReleaseId release = create("elite.zip", zip(Map.of("ELITE.ROM", ROM)));

        updateSoftware.handle(new UpdateSoftwareCommand(
                ADMIN, SOFTWARE, new SoftwareTitle("Elite"), null, null, new PlatformCode("bk"), Set.of(), Set.of()));

        assertThat(jdbc.queryForObject(
                        "SELECT executable FROM release_file WHERE release_id = ? AND file_name = 'ELITE.ROM'",
                        Boolean.class,
                        release.value()))
                .isFalse();
    }

    @Test
    void replacesAndRemovesTheStoredTreeWithTheReleaseFile() throws Exception {
        ReleaseId release = create("old.zip", zip(Map.of("OLD.ROM", ROM)));
        byte[] replacement = zip(Map.of("NEW.ROM", ROM));

        updateRelease.handle(new UpdateReleaseCommand(
                ADMIN,
                SOFTWARE,
                release,
                content(),
                Set.of(),
                upload("new.zip", replacement),
                new ReleaseArchiveRemoval(false)));

        files(release)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("new.zip"))
                .andExpect(jsonPath("$.root.children[0].name").value("NEW.ROM"))
                .andExpect(jsonPath("$.root.children[0].executable").value(true));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM release_file WHERE release_id = ? AND file_name = 'OLD.ROM'",
                        Long.class,
                        release.value()))
                .isZero();

        updateRelease.handle(new UpdateReleaseCommand(
                ADMIN, SOFTWARE, release, content(), Set.of(), null, new ReleaseArchiveRemoval(true)));

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM release_file WHERE release_id = ?", Long.class, release.value()))
                .isZero();
    }

    private ResultActions files(ReleaseId release) throws Exception {
        return mvc.perform(
                get("/api/software/{softwareId}/releases/{releaseId}/files", SOFTWARE.value(), release.value()));
    }

    private ReleaseId create(String name, byte[] content) {
        return createRelease.handle(
                new CreateReleaseCommand(ADMIN, SOFTWARE, content(), Set.of(), upload(name, content)));
    }

    private static ReleaseContent content() {
        return new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null);
    }

    private static ReleaseArchiveUpload upload(String name, byte[] content) {
        return new ReleaseArchiveUpload(name, content.length, () -> new ByteArrayInputStream(content));
    }

    private static byte[] zip(Map<String, byte[]> files) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                ZipEntry entry = new ZipEntry(file.getKey());
                entry.setTime(0);
                zip.putNextEntry(entry);
                zip.write(file.getValue());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static String md5(byte[] content) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(content));
    }

    private record FileRow(String name, @Nullable String md5, boolean directory, boolean executable) {}
}
