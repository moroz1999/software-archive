package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.label.domain.LabelName;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaImportedEntryRepository.class})
class JpaImportedEntryRepositoryIT {

    private static final ImportedEntryId SOFTWARE = new ImportedEntryId(UUID.randomUUID());
    private static final ImportedEntryId LABEL = new ImportedEntryId(UUID.randomUUID());

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JpaImportedEntryRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void writesAtOnceForPlainSqlInTheSameTransaction() {
        repository.save(new ImportedEntry(ExternalOrigin.software("vektor-06c.ru", "524"), SOFTWARE));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM external_origin WHERE external_id = '524'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void findsAnEntryByWhereItCameFrom() {
        repository.save(new ImportedEntry(ExternalOrigin.software("vektor-06c.ru", "524"), SOFTWARE));
        repository.save(new ImportedEntry(ExternalOrigin.label("vektor-06c.ru", new LabelName("Графов А.")), LABEL));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByOrigin(ExternalOrigin.software("vektor-06c.ru", "524")))
                .hasValueSatisfying(entry -> assertThat(entry.getEntry()).isEqualTo(SOFTWARE));
        assertThat(repository.findByOrigin(ExternalOrigin.label("vektor-06c.ru", new LabelName("Графов А."))))
                .hasValueSatisfying(entry -> assertThat(entry.getEntry()).isEqualTo(LABEL));
    }

    @Test
    void keepsThePageOfAnEntryInItsSourceArchive() {
        ExternalPage page = new ExternalPage("https://vektor-06c.ru/programmnoe-obespechenie/demo-programmy/512-demo/");
        repository.save(new ImportedEntry(ExternalOrigin.software("vektor-06c.ru", "524"), SOFTWARE, page));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByOrigin(ExternalOrigin.software("vektor-06c.ru", "524")))
                .hasValueSatisfying(entry -> assertThat(entry.getPage()).isEqualTo(page));
    }

    @Test
    void forgetsTheEntryOfAKindWhereverItCameFrom() {
        repository.save(new ImportedEntry(ExternalOrigin.software("vektor-06c.ru", "524"), SOFTWARE));
        repository.save(new ImportedEntry(ExternalOrigin.software("zxart.ee", "17"), SOFTWARE));
        repository.save(new ImportedEntry(ExternalOrigin.label("vektor-06c.ru", new LabelName("Графов А.")), LABEL));
        entityManager.flush();

        repository.forget(ExternalKind.SOFTWARE, SOFTWARE);
        repository.forget(ExternalKind.SOFTWARE, LABEL);
        entityManager.clear();

        assertThat(repository.findByOrigin(ExternalOrigin.software("vektor-06c.ru", "524")))
                .isEmpty();
        assertThat(repository.findByOrigin(ExternalOrigin.software("zxart.ee", "17")))
                .isEmpty();
        assertThat(repository.findByOrigin(ExternalOrigin.label("vektor-06c.ru", new LabelName("Графов А."))))
                .isPresent();
    }

    @Test
    void tellsTheSameIdOfAnotherKindOrArchiveApart() {
        repository.save(new ImportedEntry(ExternalOrigin.software("vektor-06c.ru", "524"), SOFTWARE));
        entityManager.flush();

        assertThat(repository.findByOrigin(ExternalOrigin.label("vektor-06c.ru", new LabelName("524"))))
                .isEmpty();
        assertThat(repository.findByOrigin(ExternalOrigin.software("zxart.ee", "524")))
                .isEmpty();
    }
}
