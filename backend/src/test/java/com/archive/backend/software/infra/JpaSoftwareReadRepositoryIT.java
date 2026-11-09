package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareSummary;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaSoftwareReadRepository.class})
class JpaSoftwareReadRepositoryIT {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private JpaSoftwareReadRepository reads;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void readsAPageOfSummaries() {
        insert();

        Page<SoftwareSummary> page = reads.findPage(PageRequest.of(0, 20));

        assertThat(page.getContent()).containsExactly(new SoftwareSummary(ID, "Elite", LocalDate.of(1985, 9, 20)));
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void readsTheLatestAddedSoftwareFirstWhateverSortIsAsked() {
        UUID older = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID newest = UUID.fromString("00000000-0000-0000-0000-000000000003");
        jdbc.update(
                "INSERT INTO software (id, title, created_at) VALUES (?, 'Older', TIMESTAMPTZ '2020-01-01 00:00Z'), (?, 'Newest', TIMESTAMPTZ '2024-01-01 00:00Z'), (?, 'Middle', TIMESTAMPTZ '2022-01-01 00:00Z')",
                older,
                newest,
                ID);

        Page<SoftwareSummary> page = reads.findLatest(PageRequest.of(0, 2, Sort.by("title")));

        assertThat(page.getContent()).extracting(SoftwareSummary::title).containsExactly("Newest", "Middle");
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void readsTheDetailsOfOneSoftware() {
        insert();

        assertThat(reads.findById(ID))
                .contains(new SoftwareDetails(ID, "Elite", "Space trading", LocalDate.of(1985, 9, 20)));
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(ID)).isEmpty();
    }

    private void insert() {
        jdbc.update(
                "INSERT INTO software (id, title, description, release_date) VALUES (?, 'Elite', 'Space trading', DATE '1985-09-20')",
                ID);
    }
}
