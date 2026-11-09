package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaSoftwareRepository.class, SoftwareMapperImpl.class})
class JpaSoftwareRepositoryIT {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");

    @Autowired
    private SoftwareJpaRepository jpa;

    @Autowired
    private JpaSoftwareRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesNewSoftwareUnderItsId() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                VECTOR));
        jpa.flush();

        assertThat(repository.findById(ID)).hasValueSatisfying(software -> {
            assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
            assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
            assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
            assertThat(software.getPlatform()).isEqualTo(VECTOR);
        });
        assertThat(jdbc.queryForObject(
                        "SELECT created_at IS NOT NULL FROM software WHERE id = ?", Boolean.class, ID.value()))
                .isTrue();
    }

    @Test
    void storesEdits() {
        repository.save(Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR));
        jpa.flush();
        Software software = repository.findById(ID).orElseThrow();

        software.edit(
                new SoftwareTitle("Elite Plus"), new SoftwareDescription("Enhanced"), LocalDate.of(1987, 1, 1), BK);
        repository.save(software);
        jpa.flush();

        assertThat(jdbc.queryForObject("SELECT title FROM software WHERE id = ?", String.class, ID.value()))
                .isEqualTo("Elite Plus");
        assertThat(jdbc.queryForObject("SELECT platform_code FROM software WHERE id = ?", String.class, ID.value()))
                .isEqualTo("bk");
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(repository.findById(ID)).isEmpty();
    }

    @Test
    void deletesSoftwareWithEverythingThatBelongsToIt() {
        repository.save(Software.create(ID, new SoftwareTitle("Elite"), null, null, VECTOR));
        jpa.flush();
        jdbc.update("INSERT INTO software_alternative_titles (title, software_id) VALUES ('Элита', ?)", ID.value());
        jdbc.update("INSERT INTO software_languages (software_id, language) VALUES (?, 'en')", ID.value());
        jdbc.update(
                "INSERT INTO software_links (url, type, software_id) VALUES ('https://example.com', 'homepage', ?)",
                ID.value());
        jdbc.update("INSERT INTO software_hardware (software_id, hardware_code) VALUES (?, 'vector06c')", ID.value());
        UUID release = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO software_release (id, software_id, type) VALUES (?, ?, 'original')", release, ID.value());
        jdbc.update("INSERT INTO software_release_hardware (release_id, hardware_code) VALUES (?, 'ay8910')", release);

        repository.delete(repository.findById(ID).orElseThrow());
        jpa.flush();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM software WHERE id = ?", Integer.class, ID.value()))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_alternative_titles", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_languages", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_links", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_hardware", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_release", Integer.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM software_release_hardware", Integer.class))
                .isZero();
    }
}
