package com.archive.backend.software.infra;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    void readsTheDetailsOfOneSoftware() {
        insert();

        assertThat(reads.findById(ID)).contains(new SoftwareDetails(ID, "Elite", "Space trading", LocalDate.of(1985, 9, 20)));
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(reads.findById(ID)).isEmpty();
    }

    private void insert() {
        jdbc.update("INSERT INTO software (id, title, description, release_date) VALUES (?, 'Elite', 'Space trading', DATE '1985-09-20')", ID);
    }
}
