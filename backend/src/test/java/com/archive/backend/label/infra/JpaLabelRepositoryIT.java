package com.archive.backend.label.infra;

import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaLabelRepository.class})
class JpaLabelRepositoryIT {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private JpaLabelRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesALabelOfNobodyAtOnceForPlainSqlInTheSameTransaction() {
        repository.save(Label.create(new LabelId(ID), new LabelName("Графов А."), MEMBER));

        Map<String, Object> row = jdbc.queryForMap("SELECT name, person_id, group_id FROM label WHERE id = ?", ID);
        assertThat(row.get("name")).isEqualTo("Графов А.");
        assertThat(row.get("person_id")).isNull();
        assertThat(row.get("group_id")).isNull();
    }
}
