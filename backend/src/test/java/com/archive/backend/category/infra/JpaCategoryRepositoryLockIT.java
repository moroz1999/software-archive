package com.archive.backend.category.infra;

import static com.archive.backend.category.infra.SeededTree.DEMOSCENE;
import static com.archive.backend.category.infra.SeededTree.INTRO;
import static com.archive.backend.category.infra.SeededTree.INTRO_4K;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.domain.CategoryId;
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
 * A category is loaded for a change under a lock held until the change commits, and so is the line of the parent it is
 * checked against: two moves cannot put two categories under each other (docs/backend.md).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    TestcontainersConfiguration.class,
    JpaCategoryRepository.class,
    CategoryMapperImpl.class,
    JdbcCategoryHierarchy.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaCategoryRepositoryLockIT {

    @Autowired
    private JpaCategoryRepository repository;

    @Autowired
    private JdbcCategoryHierarchy hierarchy;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactions;

    private TransactionTemplate transaction;
    private TransactionTemplate another;

    @BeforeEach
    void prepareTransactions() {
        transaction = new TransactionTemplate(transactions);
        another = new TransactionTemplate(transactions);
        another.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    private void cannotLoadInAnother(CategoryId id) {
        assertThatThrownBy(() -> another.executeWithoutResult(inner -> {
                    jdbc.execute("SET LOCAL lock_timeout = '200ms'");
                    repository.findById(id);
                }))
                .isInstanceOf(PessimisticLockingFailureException.class);
    }

    @Test
    void anotherChangeCannotLoadTheCategoryUntilTheFirstCommits() {
        transaction.executeWithoutResult(status -> {
            assertThat(repository.findById(new CategoryId(INTRO))).isPresent();

            cannotLoadInAnother(new CategoryId(INTRO));
        });
    }

    @Test
    void anotherChangeCannotLoadACategoryOfTheCheckedLineUntilTheFirstCommits() {
        transaction.executeWithoutResult(status -> {
            assertThat(hierarchy.lineOf(new CategoryId(INTRO_4K))).isPresent();

            cannotLoadInAnother(new CategoryId(DEMOSCENE));
        });
    }
}
