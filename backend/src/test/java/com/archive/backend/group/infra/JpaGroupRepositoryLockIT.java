package com.archive.backend.group.infra;

import static com.archive.backend.user.TestActors.BY_OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupMembers;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.domain.GroupTypeSet;
import com.archive.backend.user.TestActors;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** A group is loaded for a change under a lock held until the change commits (docs/backend.md). */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@IntegrationTest
class JpaGroupRepositoryLockIT {

    private static final GroupId ID = new GroupId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Autowired
    private JpaGroupRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactions;

    private TransactionTemplate transaction;
    private TransactionTemplate another;

    @BeforeEach
    void storeGroup() {
        TestActors.store(jdbc);
        transaction = new TransactionTemplate(transactions);
        another = new TransactionTemplate(transactions);
        another.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> repository.save(Group.create(
                ID,
                new GroupName("Crew Ltd"),
                null,
                GroupTypeSet.restore(Set.of(new GroupTypeCode("demogroup"))),
                List.of(),
                new Locations(List.of()),
                GroupMembers.EMPTY,
                BY_OTHER)));
    }

    @AfterEach
    void removeGroup() {
        jdbc.update("DELETE FROM \"group\" WHERE id = ?", ID.value());
        TestActors.remove(jdbc);
    }

    @Test
    void anotherChangeCannotLoadTheGroupUntilTheFirstCommits() {
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
