package com.archive.backend.software.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.category.infra.JdbcCategoryCatalog;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Software is loaded for a change under a lock held until the change commits: two changes of one software never check
 * its invariants against the same state (docs/backend.md). Each transaction here is real and commits, so the test runs
 * outside the transaction of the test itself.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    TestcontainersConfiguration.class,
    JpaSoftwareRepository.class,
    SoftwareMapperImpl.class,
    JdbcCategoryCatalog.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaSoftwareRepositoryLockIT {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Autowired
    private JpaSoftwareRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactions;

    private TransactionTemplate transaction;

    /** Another transaction on its own connection that waits for a lock no longer than a moment. */
    private TransactionTemplate another;

    @BeforeEach
    void storeSoftware() {
        transaction = new TransactionTemplate(transactions);
        another = new TransactionTemplate(transactions);
        another.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                HardwareSet.restore(new PlatformCode("vector"), Set.of()),
                CategorySet.EMPTY)));
    }

    @AfterEach
    void removeSoftware() {
        jdbc.update("DELETE FROM software WHERE id = ?", ID.value());
    }

    private void waitForLocksOnlyAMoment() {
        jdbc.execute("SET LOCAL lock_timeout = '200ms'");
    }

    @Test
    void anotherChangeCannotLoadTheSoftwareUntilTheFirstCommits() {
        transaction.executeWithoutResult(status -> {
            assertThat(repository.findById(ID)).isPresent();

            assertThatThrownBy(() -> another.executeWithoutResult(inner -> {
                        waitForLocksOnlyAMoment();
                        repository.findById(ID);
                    }))
                    .isInstanceOf(PessimisticLockingFailureException.class);
        });
    }

    @Test
    void anotherChangeLoadsTheSoftwareOnceTheFirstHasCommitted() {
        transaction.executeWithoutResult(
                status -> assertThat(repository.findById(ID)).isPresent());

        Software loaded = another.execute(inner -> {
            waitForLocksOnlyAMoment();
            return repository.findById(ID).orElseThrow();
        });

        assertThat(loaded).isNotNull();
    }

    @Test
    void readingIsNotBlockedByAChange() {
        transaction.executeWithoutResult(status -> {
            assertThat(repository.findById(ID)).isPresent();

            String title = another.execute(inner -> {
                waitForLocksOnlyAMoment();
                return jdbc.queryForObject("SELECT title FROM software WHERE id = ?", String.class, ID.value());
            });

            assertThat(title).isEqualTo("Elite");
        });
    }
}
