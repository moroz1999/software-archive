package com.archive.backend.label.infra;

import com.archive.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** What the schema itself keeps true about labels (docs/domain/authors.md). */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class LabelTableIT {

    private static final UUID PERSON = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GROUP = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        jdbc.update("INSERT INTO person (id) VALUES (?)", PERSON);
        jdbc.update("INSERT INTO \"group\" (id, name) VALUES (?, 'Crew')", GROUP);
    }

    @Test
    void aLabelBelongsToNobodyToAPersonOrToAGroup() {
        int inserted = jdbc.update("INSERT INTO label (name, person_id, group_id) VALUES ('Ghost', NULL, NULL), ('Zed', ?, NULL), ('CRW', NULL, ?)",
                PERSON, GROUP);

        assertThat(inserted).isEqualTo(3);
    }

    @Test
    void aLabelNeverBelongsToAPersonAndAGroupAtOnce() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO label (name, person_id, group_id) VALUES ('Both', ?, ?)", PERSON, GROUP))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aGroupIsNeverInsideItself() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO group_member_group (group_id, member_group_id) VALUES (?, ?)", GROUP, GROUP))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void labelsAreLocatedAsLabels() {
        UUID label = UUID.randomUUID();
        jdbc.update("INSERT INTO label (id, name) VALUES (?, 'Ghost')", label);

        assertThat(jdbc.update("INSERT INTO entity_location (entity_type, entity_id) VALUES ('LABEL', ?)", label)).isEqualTo(1);
    }
}
