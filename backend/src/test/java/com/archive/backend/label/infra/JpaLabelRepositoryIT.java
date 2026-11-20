package com.archive.backend.label.infra;

import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.user.TestActors;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JpaLabelRepositoryIT {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private JpaLabelRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void storeActors() {
        TestActors.store(jdbc);
    }

    @Test
    void storesALabelOfNobodyAtOnceForPlainSqlInTheSameTransaction() {
        repository.save(Label.create(new LabelId(ID), new LabelName("Графов А."), new Locations(List.of()), MEMBER));

        Map<String, Object> row = jdbc.queryForMap("SELECT name, person_id, group_id FROM label WHERE id = ?", ID);
        assertThat(row.get("name")).isEqualTo("Графов А.");
        assertThat(row.get("person_id")).isNull();
        assertThat(row.get("group_id")).isNull();
    }

    @Test
    void findsALabelOfNobodyButNotALabelOfAnOwner() {
        UUID owned = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID person = UUID.fromString("00000000-0000-0000-0000-000000000003");
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", person);
        jdbc.update(
                "INSERT INTO label (id, name, person_id) VALUES (?, 'Nobody', NULL), (?, 'JD', ?)", ID, owned, person);

        assertThat(repository.lockOfNobody(new LabelId(ID)))
                .hasValueSatisfying(label -> assertThat(label.getName()).isEqualTo(new LabelName("Nobody")));
        assertThat(repository.lockOfNobody(new LabelId(owned))).isEmpty();
        assertThat(repository.exists(new LabelId(owned))).isTrue();
        assertThat(repository.exists(new LabelId(UUID.randomUUID()))).isFalse();
    }
}
