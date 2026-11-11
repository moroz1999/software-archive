package com.archive.backend.author.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.domain.Locations;
import java.util.List;
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

/** An author is loaded for a change under a lock held until the change commits (docs/backend.md). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaAuthorRepository.class, AuthorMapperImpl.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaAuthorRepositoryLockIT {

    private static final AuthorId ID = new AuthorId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Autowired
    private JpaAuthorRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactions;

    private TransactionTemplate transaction;
    private TransactionTemplate another;

    @BeforeEach
    void storeAuthor() {
        transaction = new TransactionTemplate(transactions);
        another = new TransactionTemplate(transactions);
        another.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> repository.save(
                Author.create(ID, new RealName("John Doe"), null, List.of(), new Locations(List.of()))));
    }

    @AfterEach
    void removeAuthor() {
        jdbc.update("DELETE FROM person WHERE id = ?", ID.value());
    }

    @Test
    void anotherChangeCannotLoadTheAuthorUntilTheFirstCommits() {
        transaction.executeWithoutResult(status -> {
            assertThat(repository.findById(ID)).isPresent();

            assertThatThrownBy(() -> another.executeWithoutResult(inner -> {
                        jdbc.execute("SET LOCAL lock_timeout = '200ms'");
                        repository.findById(ID);
                    }))
                    .isInstanceOf(PessimisticLockingFailureException.class);
        });
    }
}
