package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.importer.read.ExternalOriginReadRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JdbcExternalOriginReadRepositoryIT {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String PAGE = "https://zxart.ee/eng/software/game/elite/";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ExternalOriginReadRepository reads;

    @Test
    void findsTheEntryOfAKindByItsIdInItsSourceArchive() {
        jdbc.update(
                "INSERT INTO external_origin (origin, kind, external_id, entity_id, url) VALUES ('zxart.ee', 'SOFTWARE', ?, ?, ?)",
                PAGE,
                ELITE,
                PAGE);

        assertThat(reads.findEntity("zxart.ee", "SOFTWARE", PAGE)).contains(ELITE);
        assertThat(reads.findEntity("zxart.ee", "LABEL", PAGE)).isEmpty();
        assertThat(reads.findEntity("vtrd.in", "SOFTWARE", PAGE)).isEmpty();
    }
}
